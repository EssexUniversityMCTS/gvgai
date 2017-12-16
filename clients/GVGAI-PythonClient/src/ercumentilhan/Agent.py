"""
This controller is based on a shallow reinforcement learning method, true online Sarsa(lambda) with linear function
approximation, dutch traces and softmax/epsilon-greedy action selection policy [1]. In addition, "Double" and "Expected"
variants of Sarsa algorithm are employed [2].

States are represented with hand-designed features which are comprised of normalized relative proximity values between
the closest (with respect to Manhattan distance) observations of item type pairs.

[1] Sutton, R.S. and Barto, A.G. (2017) Reinforcement Learning: An Introduction. Second edition (Draft), MIT press,
Cambridge. (Chapter 12)

[2] Ganger, M., Duryea, E. and Hu, W. (2016) Double Sarsa and Double Expected Sarsa with Shallow and Deep Learning.
Journal of Data Analysis and Information Processing, 4, 159-176.
"""

from __future__ import print_function
import random
from AbstractPlayer import AbstractPlayer
from utils.Types import LEARNING_SSO_TYPE
from utils.SerializableStateObservation import Observation
import math
import numpy as np
from past.builtins import xrange


class Agent(AbstractPlayer):

    def __init__(self):
        """
        Method to be called at the start of an evaluation process (training & testing) for a particular game.
        """
        AbstractPlayer.__init__(self)

        # Parameters (to be tuned)
        self.alpha_base = 0.05  # Learning rate base, is used for determining the learning rate adaptively.
        self.gamma = 0.99  # Discount factor
        self.lambd = 0.5  # Decay rate
        self.loss_reward = 0.0  # Extra reward for the transitions leading to a loss (if within time limit)
        self.win_reward = 1.0  # Extra reward for the transitions leading to a win (if within time limit)
        self.action_selection_policy = 1  # Action selection policy (0: Softmax, 1: Epsilon-greedy)
        self.epsilon_start = 1.0  # Epsilon start value for epsilon-greedy policy
        self.epsilon_final = 0.05  # Epsilon final value for epsilon-greedy policy
        self.epsilon_decay = 500  # Epsilon decay rate in terms of steps for epsilon-greedy policy
        self.print_information = True  # Print game outcomes

        # Other variables
        self.alpha = None  # Learning rate (to be determined)

        self.item_types = set()  # A set to hold observed item types (itype)
        self.item_pairs_dict = []  # Dictionary of item type pairs

        self.running_mode = 0  # Indicates the phase of training. 0: Full exploration (first 3 levels), 1: Learning

        self.lastSsoType = LEARNING_SSO_TYPE.JSON
        self.blockSize = None
        self.worldDimension = None
        self.maximumDistance = None
        self.n_actions = None
        self.max_dimensions = [0.0, 0.0]
        self.max_distance = None

        self.previous_gameScore = None

        self.w = [None, None]  # Two set of weights (Double Sarsa)
        self.e = None
        self.q_old = None

        self.f_length_single = 4  # Length of a feature component (is fixed)
        self.f_length_full = None

        self.previous_state_features = None
        self.previous_state_features_extended = None
        self.previous_action = None

        self.n_plays = 0
        self.n_wins = 0
        self.n_losses = 0
        self.n_total_steps = 0
        self.average_score = 0.0

    def init(self, sso, elapsed_timer):
        """
        Method to be called at the start of every level of a game.

        :param sso: observation of the current state of the game
        :param elapsed_timer: the timer
        :return:
        """

        # Level specific variables, re-initialize at the start of every level
        self.blockSize = sso.blockSize
        self.worldDimension = sso.worldDimension
        self.maximumDistance = self.worldDimension[0] + self.worldDimension[1]
        self.n_actions = len(sso.availableActions)

        self.previous_state_features = None
        self.previous_state_features_extended = None
        self.previous_action = None

        self.previous_gameScore = 0

        if self.n_plays == 3:  # Initial exploration is over, switch to the learning mode (0->1), initialize variables

            self.running_mode = 1

            self.max_distance = self.max_dimensions[0] + self.max_dimensions[1]

            self.generate_dictionary()
            self.f_length_full = self.f_length_single * len(self.item_pairs_dict)

            # Initialize the weights
            for i, _ in enumerate(self.w):
                self.w[i] = np.zeros((self.f_length_full*self.n_actions,), dtype=np.float32)

            self.e = np.zeros((self.f_length_full*self.n_actions,), dtype=np.float32)
            self.q_old = 0

        if self.running_mode == 0:

            # Determine the maximum dimensions
            dimensions = [self.worldDimension[0]/self.blockSize, self.worldDimension[1]/self.blockSize]
            if dimensions[0] > self.max_dimensions[0]:
                self.max_dimensions[0] = dimensions[0]
            if dimensions[1] > self.max_dimensions[1]:
                self.max_dimensions[1] = dimensions[1]

    def learn(self, state_features, reward, action_probabilities):
        """
        Executes a step of the learning algorithm.

        :param state_features: state features
        :param reward: transition reward
        :param action_probabilities: action probabilities from the "state_features" according to the current policy
        """
        q_previous = self.w[0].dot(self.previous_state_features_extended)

        # State value calculation (Expected Sarsa)
        q = 0.0
        for i_action, action_probability in enumerate(action_probabilities):
            q += (action_probability * self.w[1].dot(self.extend_features(state_features, i_action)))

        delta = reward + self.gamma * q - q_previous

        self.e = self.gamma * self.lambd * self.e + \
                 (1.0 - self.alpha * self.gamma * self.lambd * (self.e.dot(self.previous_state_features_extended))) * \
                 self.previous_state_features_extended

        self.w[0] += self.alpha * (delta + q_previous - self.q_old) * self.e - \
                     self.alpha * (q_previous - self.q_old) * self.previous_state_features_extended

        self.q_old = q

        # Keep the weights within a reasonable range to prevent possible errors
        np.clip(self.w[0], -10.0, 10.0, out=self.w[0])

        # Swap weights with probability 0.5 (Double Sarsa)
        if random.randint(0, 1):
            self.w[0], self.w[1] = self.w[1], self.w[0]

    def act(self, sso, elapsed_timer):
        """
        Method used to determine the next move to be performed by the agent.

        :param sso: observation of the current state of the game
        :param elapsed_timer: the timer
        :return: index of the action to be taken
        """
        # Training mode
        if self.running_mode == 1:

            reward = sso.gameScore - self.previous_gameScore
            state_features = self.extract_features(sso)
            action, action_probabilities = self.select_action(state_features)
            state_features_extended = self.extend_features(state_features, action)

            if sso.gameTick > 0:
                # Perform a step of learning for the previous transition
                self.learn(state_features, reward, action_probabilities)

            self.previous_state_features = np.copy(state_features)
            self.previous_state_features_extended = np.copy(state_features_extended)
            self.previous_action = action

            self.n_total_steps += 1

        # Initial exploration mode: scan state observations for item types, select actions randomly
        elif self.running_mode == 0:

            self.scan_observations(sso)

            if sso.gameTick == 200:  # Terminate at step 200
                return "ACTION_ESCAPE"

            self.previous_action = random.randint(0, self.n_actions - 1)

        self.previous_gameScore = sso.gameScore

        return sso.availableActions[self.previous_action]

    def softmax(self, x):
        """
        Converts an input vector into another same sized output vector of real values in the range [0,1] that add up to
        1.

        :param x: input vector
        :return: output vector
        """
        e_x = np.exp(x - np.max(x))
        return e_x / e_x.sum(axis=0)

    def select_action(self, state_features):
        """
        Determines the action to be taken based on state features by using softmax policy.

        :param state_features: state features
        :return: selected action, probabilities for the actions
        """
        action_values = np.zeros((self.n_actions,), dtype=np.float32)

        for i_action in xrange(self.n_actions):
            features_extended = self.extend_features(state_features, i_action)
            # Use both sets of the weights (Double Sarsa)
            action_values[i_action] = (self.w[0].dot(features_extended) + self.w[1].dot(features_extended))

        selected_action = None
        random_number = random.uniform(0.0, 1.0)

        if self.action_selection_policy == 0:  # Softmax
            action_probabilities = self.softmax(action_values)
            action_probabilities_cs = np.cumsum(action_probabilities)
            for i_action in xrange(self.n_actions):
                if random_number <= action_probabilities_cs[i_action]:
                    selected_action = i_action
                    break

        elif self.action_selection_policy == 1:  # Epsilon-greedy
            epsilon = self.epsilon_final + (self.epsilon_start - self.epsilon_final)*\
                      math.exp(-1.0*self.n_total_steps/self.epsilon_decay)

            action_probabilities = np.zeros((self.n_actions,), dtype=np.float32)

            greedy_action = np.argmax(action_values)
            action_probabilities[greedy_action] = 1.0 - epsilon
            action_probabilities[action_probabilities == 0.0] = epsilon/(self.n_actions - 1)

            if random_number < epsilon:  # Random action
                selected_action = random.randint(0, self.n_actions - 1)
            else:  # Greedy action
                selected_action = greedy_action

        return selected_action, action_probabilities

    def scan_observations(self, sso):
        """
        Scans the given state observation and adds the observed item types to the set.

        :param sso: observation of the current state of the game
        """
        if sso.avatarType != 0:
            self.item_types.add(sso.avatarType)
        for observation in sso.NPCPositions:
            self.item_types.add(observation[0].itype)
        for observation in sso.immovablePositions:
            self.item_types.add(observation[0].itype)
        for observation in sso.movablePositions:
            self.item_types.add(observation[0].itype)
        for observation in sso.resourcesPositions:
            self.item_types.add(observation[0].itype)
        for observation in sso.portalsPositions:
            self.item_types.add(observation[0].itype)
        for observation in sso.fromAvatarSpritesPositions:
            self.item_types.add(observation[0].itype)

    def generate_dictionary(self):
        """
        Generates the dictionary of item type pairs by using the item types set.
        Avatar-Avatar pair is possibly redundant, yet is kept for the sake of generality.
        """
        item_types_list = list(self.item_types)
        item_types_list.sort()

        for i in range(len(item_types_list)):
            for j in range(i, len(item_types_list)):
                self.item_pairs_dict.append((item_types_list[i], item_types_list[j]))

    def extract_features(self, sso):
        """
        Extracts a set of features from a given state observation.

        :param sso: observation of the current state of the game
        :return: state features vector
        """
        features = np.zeros((self.f_length_full,), dtype=np.float32)

        all_observations = []

        # Creating an avatar observation to process it in standardized way
        avatar_observation = Observation()
        avatar_observation.itype = sso.avatarType
        avatar_observation.position.x = sso.avatarPosition[0]
        avatar_observation.position.y = sso.avatarPosition[1]

        all_observations.append([avatar_observation])

        for observations in sso.NPCPositions:
            if observations[0].itype in self.item_types:
                all_observations.append(observations)
        for observations in sso.immovablePositions:
            if observations[0].itype in self.item_types:
                all_observations.append(observations)
        for observations in sso.movablePositions:
            if observations[0].itype in self.item_types:
                all_observations.append(observations)
        for observations in sso.resourcesPositions:
            if observations[0].itype in self.item_types:
                all_observations.append(observations)
        for observations in sso.portalsPositions:
            if observations[0].itype in self.item_types:
                all_observations.append(observations)
        for observations in sso.fromAvatarSpritesPositions:
            if observations[0].itype in self.item_types:
                all_observations.append(observations)

        for i in range(len(all_observations)):
            item_type_1 = all_observations[i][0].itype

            for j in range(i, len(all_observations)):
                item_type_2 = all_observations[j][0].itype

                # A feature component (represents a single item type pair) is comprised of 4 elements which stand for
                # the normalized relative proximities in the horizontal (-x, +x) and vertical (-y, +y) paths
                # (in this order).

                features_single = np.zeros((4,), dtype=np.float32)

                min_distance = float("inf")
                min_distances = [self.max_dimensions[0], self.max_dimensions[1]]

                for ii in range(len(all_observations[i])):
                    if all_observations[i][ii] is not None:
                        for jj in range(len(all_observations[j])):
                            if all_observations[j][jj] is not None:
                                if all_observations[i][ii].obsID != all_observations[j][jj].obsID:
                                    distances = [(all_observations[j][jj].position.x-all_observations[i][ii].position.x)
                                                 / self.blockSize,
                                                 (all_observations[j][jj].position.y-all_observations[i][ii].position.y)
                                                 / self.blockSize]

                                    # Manhattan distance
                                    distance = abs(distances[0]) + abs(distances[1])

                                    # Update for the closest observation pair
                                    if distance < min_distance:
                                        min_distance = distance
                                        min_distances = [distances[0], distances[1]]

                # Minimum distance -> proximity
                normalized_proximities = np.array([1.0 - abs(min_distances[0] / self.max_dimensions[0]),
                                                     1.0 - abs(min_distances[1] / self.max_dimensions[1])])
                normalized_proximities[normalized_proximities < 0] = 0.0

                if min_distances[0] < 0.0:
                    features_single[0] = normalized_proximities[0]
                elif min_distances[0] > 0.0:
                    features_single[1] = normalized_proximities[0]
                elif min_distances[0] == 0.0:
                    features_single[0] = normalized_proximities[0]
                    features_single[1] = normalized_proximities[0]

                if min_distances[1] < 0.0:
                    features_single[2] = normalized_proximities[1]
                elif min_distances[1] > 0.0:
                    features_single[3] = normalized_proximities[1]
                elif min_distances[1] == 0.0:
                    features_single[2] = normalized_proximities[1]
                    features_single[3] = normalized_proximities[1]

                # Determine the dictionary index of current item types pair
                target_tuple = (item_type_1, item_type_2)
                if item_type_2 < item_type_1:
                    target_tuple = (item_type_2, item_type_1)

                f_index = self.item_pairs_dict.index(target_tuple)

                # Determine the slice position of corresponding item types pair in the complete feature vector
                f_start = f_index * self.f_length_single
                f_end = f_start + self.f_length_single

                # Put the features in the complete feature vector accordingly
                features[f_start:f_end] = np.copy(features_single)

        # For the first time a feature vector is computed, determine alpha (learning rate) by diving a constant
        # parameter by L2 norm of this feature vector. This is a very naive way to set an adaptive learning rate.
        if self.alpha is None:
            self.alpha = self.alpha_base/np.linalg.norm(features, ord=2)

        return features

    def extend_features(self, state_features, action_index):
        """
        Extends the given feature vector by multiplying its size and filling up with zeros for the other actions.

        :param state_features: state features
        :param action_index: index of relevant action
        :return: extended state features vector
        """
        extended_state_features = np.zeros((self.n_actions*self.f_length_full,), dtype=np.float32)
        if action_index is None:
            return extended_state_features
        partition_start = action_index * self.f_length_full
        partition_end = partition_start + self.f_length_full
        extended_state_features[partition_start:partition_end] = np.copy(state_features)
        return extended_state_features

    def result(self, sso, elapsed_timer):
        """
        Method used to perform actions in case of a game end.
        This is the last thing called when a level is played (the game is already in a terminal state).

        :param sso: observation of the current state of the game
        :param elapsed_timer: the timer
        :return: id of the next level to be played
        """
        self.n_plays += 1

        if self.running_mode == 1:

            reward = sso.gameScore - self.previous_gameScore

            # Reward modifications for transitions that lead to a termination within time limit
            if sso.gameTick < 2000:
                if sso.gameWinner == 'PLAYER_LOSES':
                    reward += self.loss_reward
                elif sso.gameWinner == 'PLAYER_WINS':
                    reward += self.win_reward

            # Terminal state: features and action probabilities are all set to zero
            features = np.zeros((self.f_length_full,), dtype=np.float32)
            action_probabilities = np.zeros((self.n_actions,), dtype=np.float32)

            self.learn(features, reward, action_probabilities)

            if self.print_information:

                self.average_score += (sso.gameScore - self.average_score) / (self.n_plays - 3)

                if sso.gameWinner == 'PLAYER_LOSES':
                    self.n_losses += 1
                    print(' LOSS |',sso.gameScore,'|',int(self.average_score),'| (',self.n_wins,'-',self.n_losses,')')
                elif sso.gameWinner == 'PLAYER_WINS':
                    self.n_wins += 1
                    print('> WIN |',sso.gameScore,'|',int(self.average_score),'| (',self.n_wins,'-',self.n_losses,')')

        return self.n_plays%3  # Cycle through 3 levels.
