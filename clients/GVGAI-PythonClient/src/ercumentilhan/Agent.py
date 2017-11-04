import random
from AbstractPlayer import AbstractPlayer
from Types import *
from utils.Types import LEARNING_SSO_TYPE
import sys, os, math
import numpy as np

########################################################################################################################

class Agent(AbstractPlayer):

    def __init__(self):
        AbstractPlayer.__init__(self)

        self.w = [None, None]

        self.Alpha = 0.025
        self.Gamma = 0.99
        self.Lambda = 0.9
        self.loss_reward = 0.0
        self.win_reward = 1.0

        self.item_types = set()
        self.item_pairs_dict = []

        self.RUNNING_MODE = 0

        self.f_length_single = None
        self.f_length_full = None

        self.max_dimensions = [0.0, 0.0]
        self.max_distance = None

        self.n_plays = 0
        self.n_wins = 0
        self.n_losses = 0
        self.total_score = 0.0
        self.average_score = 0.0

        self.lastSsoType = LEARNING_SSO_TYPE.JSON
        self.blockSize = None
        self.worldDimension = None
        self.maximumDistance = None
        self.n_actions = None

        self.e = None
        self.q_old = None

        self.features_previous = None
        self.features_extended_previous = None
        self.action_previous = None
        self.gameScore_previous = None

        self.feature_norm = None

    ####################################################################################################################

    def init(self, sso, elapsed_timer):

        self.blockSize = sso.blockSize
        self.worldDimension = sso.worldDimension
        self.maximumDistance = self.worldDimension[0] + self.worldDimension[1]
        self.n_actions = len(sso.availableActions)

        self.features_previous = None
        self.features_extended_previous = None
        self.action_previous = None

        self.gameScore_previous = 0

        if self.RUNNING_MODE == 1:
            self.f_length_single = 4
            self.f_length_full = self.f_length_single * len(self.item_pairs_dict)

            for i in xrange(2):
                if self.w[i] is None:
                    self.w[i] = np.zeros((self.f_length_full*self.n_actions,), dtype=np.float32)

            self.e = np.zeros((self.f_length_full*self.n_actions,), dtype=np.float32)
            self.q_old = 0

        elif self.RUNNING_MODE == 0:
            # Determine maximum dimensions
            dimensions = [self.worldDimension[0]/self.blockSize, self.worldDimension[1]/self.blockSize]
            if dimensions[0] > self.max_dimensions[0]:
                self.max_dimensions[0] = dimensions[0]
            if dimensions[1] > self.max_dimensions[1]:
                self.max_dimensions[1] = dimensions[1]

    ####################################################################################################################

    def act(self, sso, elapsed_timer):

        if self.RUNNING_MODE == 1:

            reward = sso.gameScore - self.gameScore_previous
            features = self.extractFeatures(sso)
            action, action_probabilities = self.selectAction(features)
            features_extended = self.extendFeatures(features, action)

            if sso.gameTick > 0:

                q_previous = self.w[0].dot(self.features_extended_previous)

                q = 0.0
                for i_action in xrange(self.n_actions):
                    q += (action_probabilities[i_action] * self.w[1].dot(self.extendFeatures(features, i_action)))

                delta = reward + self.Gamma*q - q_previous

                self.e = self.Gamma*self.Lambda*self.e + \
                         (1.0 - self.Alpha*self.Gamma*self.Lambda*(self.e.dot(self.features_extended_previous))) * \
                         self.features_extended_previous

                self.w[0] += self.Alpha*(delta + q_previous - self.q_old)*self.e - \
                          self.Alpha*(q_previous - self.q_old)*self.features_extended_previous

                self.q_old = q

                np.clip(self.w[0], -10.0, 10.0, out=self.w[0])

                if random.randint(0, 1) == 1:
                    self.w[0], self.w[1] = self.w[1], self.w[0]

            self.features_previous = np.copy(features)
            self.features_extended_previous = np.copy(features_extended)
            self.action_previous = action

        elif self.RUNNING_MODE == 0:
            self.scanObservations(sso)
            self.action_previous = random.randint(0, self.n_actions - 1)

            if sso.gameTick == 200:
                return "ACTION_ESCAPE"

        self.gameScore_previous = sso.gameScore

        return sso.availableActions[self.action_previous]

    ####################################################################################################################

    def softmax(self, x):
        e_x = np.exp(x - np.max(x))
        return e_x / e_x.sum(axis=0)

    ####################################################################################################################

    def selectAction(self, S):
        x = np.zeros((self.n_actions,), dtype=np.float32)

        for i_action in xrange(self.n_actions):
            S_a = self.extendFeatures(S, i_action)
            x[i_action] = (self.w[0].dot(S_a) + self.w[1].dot(S_a))

        x_softmax = self.softmax(x)
        x_softmax_cs = np.cumsum(x_softmax)

        #print x, '\n', x_softmax, '\n'

        selected_action_index = None
        random_number = random.uniform(0.0, 1.0)

        for i_action in xrange(self.n_actions):
            if random_number <= x_softmax_cs[i_action]:
                selected_action_index = i_action
                break

        self.previous_string = str(x) + '\n' + str(x_softmax) + '\n' + str(selected_action_index)

        return selected_action_index, x_softmax

    ####################################################################################################################

    def scanObservations(self, sso):
        observations = []
        observations.append(sso.NPCPositions)
        observations.append(sso.immovablePositions)
        observations.append(sso.movablePositions)
        observations.append(sso.resourcesPositions)
        observations.append(sso.portalsPositions)
        observations.append(sso.fromAvatarSpritesPositions)
        for i in range(len(observations)):
            for j in range(len(observations[i])):
                self.item_types.add(observations[i][j][0].itype)

    ####################################################################################################################

    def generateDictionary(self):

        item_types_list = list(self.item_types)
        item_types_list.sort()

        for i in range(len(item_types_list)):
            self.item_pairs_dict.append((-1, item_types_list[i]))

        for i in range(len(item_types_list)):
            for j in range(i+1, len(item_types_list)):
                self.item_pairs_dict.append((item_types_list[i], item_types_list[j]))

    ####################################################################################################################

    def extractFeatures(self, sso):

        features = np.zeros((self.f_length_full,), dtype=np.float32)

        observations = []
        observations.append(sso.NPCPositions)
        observations.append(sso.immovablePositions)
        observations.append(sso.movablePositions)
        observations.append(sso.resourcesPositions)
        observations.append(sso.portalsPositions)
        observations.append(sso.fromAvatarSpritesPositions)

        obs = []
        for i in range(len(observations)):
            for j in range(len(observations[i])):
                if observations[i][j][0].itype in self.item_types:
                    obs.append(observations[i][j])

        item_type_1 = -1
        for j in range(len(obs)):
            item_type_2 = obs[j][0].itype
            features_single = np.zeros((4,), dtype=np.float32)

            min_distance = self.max_distance
            min_dimensions = [self.max_dimensions[0], self.max_dimensions[1]]

            for jj in range(len(obs[j])):
                if obs[j][jj] is not None:
                    dimensions = [(obs[j][jj].position.x - sso.avatarPosition[0]) / self.blockSize,
                                  (obs[j][jj].position.y - sso.avatarPosition[1]) / self.blockSize]

                    distance = abs(dimensions[0]) + abs(dimensions[1])

                    if distance < min_distance:
                        min_distance = distance
                        min_dimensions = [dimensions[0], dimensions[1]]

            normalized_dimension = 1.0 - abs(min_dimensions[0] / self.max_dimensions[0])
            if min_dimensions[0] < 0.0:
                features_single[0] = normalized_dimension
            elif min_dimensions[0] > 0.0:
                features_single[1] = normalized_dimension
            elif min_dimensions[0] == 0.0:
                features_single[0] = normalized_dimension
                features_single[1] = normalized_dimension

            normalized_dimension = 1.0 - abs(min_dimensions[1] / self.max_dimensions[1])
            if min_dimensions[1] < 0.0:
                features_single[2] = normalized_dimension
            elif min_dimensions[1] > 0.0:
                features_single[3] = normalized_dimension
            elif min_dimensions[1] == 0.0:
                features_single[2] = normalized_dimension
                features_single[3] = normalized_dimension

            f_index = self.item_pairs_dict.index((item_type_1, item_type_2))
            f_start = f_index * self.f_length_single
            f_end = f_start + self.f_length_single
            features[f_start:f_end] = np.copy(features_single)

        for i in range(len(obs)):
            item_type_1 = obs[i][0].itype

            for j in range(i+1, len(obs)):
                item_type_2 = obs[j][0].itype
                features_single = np.zeros((4,), dtype=np.float32)

                min_distance = self.max_distance
                min_dimensions = [self.max_dimensions[0], self.max_dimensions[1]]

                for ii in range(len(obs[i])):
                    if obs[i][ii] is not None:
                        for jj in range(len(obs[j])):
                            if obs[j][jj] is not None:
                                dimensions = [(obs[j][jj].position.x - obs[i][ii].position.x) / self.blockSize,
                                              (obs[j][jj].position.y - obs[i][ii].position.y) / self.blockSize]

                                distance = abs(dimensions[0]) + abs(dimensions[1])

                                if distance < min_distance:
                                    min_distance = distance
                                    min_dimensions = [dimensions[0], dimensions[1]]

                normalized_dimension = 1.0 - abs(min_dimensions[0] / self.max_dimensions[0])
                if min_dimensions[0] < 0.0:
                    features_single[0] = normalized_dimension
                elif min_dimensions[0] > 0.0:
                    features_single[1] = normalized_dimension
                elif min_dimensions[0] == 0.0:
                    features_single[0] = normalized_dimension
                    features_single[1] = normalized_dimension

                normalized_dimension = 1.0 - abs(min_dimensions[1] / self.max_dimensions[1])
                if min_dimensions[1] < 0.0:
                    features_single[2] = normalized_dimension
                elif min_dimensions[1] > 0.0:
                    features_single[3] = normalized_dimension
                elif min_dimensions[1] == 0.0:
                    features_single[2] = normalized_dimension
                    features_single[3] = normalized_dimension

                target_tuple = None
                if item_type_1 < item_type_2:
                    target_tuple = (item_type_1, item_type_2)
                else:
                    target_tuple = (item_type_2, item_type_1)

                f_index = self.item_pairs_dict.index(target_tuple)

                f_start = f_index * self.f_length_single
                f_end = f_start + self.f_length_single

                features[f_start:f_end] = np.copy(features_single)

                if self.feature_norm == None:
                    self.feature_norm = np.linalg.norm(features, ord=2)
                    self.Alpha = 0.075 / self.feature_norm
                    #print 'Alpha', self.Alpha

        return features

    ####################################################################################################################

    def extendFeatures(self, features, action_index):
        extended_features = np.zeros((self.n_actions*self.f_length_full,), dtype=np.float32)
        if action_index is None:
            return extended_features
        partition_start = action_index * self.f_length_full
        partition_end = partition_start + self.f_length_full
        extended_features[partition_start:partition_end] = np.copy(features)
        return extended_features

    ####################################################################################################################

    def result(self, sso, elapsedTimer):

        self.n_plays += 1

        if self.RUNNING_MODE == 1:

            self.total_score += sso.gameScore
            self.average_score += ((sso.gameScore - self.average_score)/(self.n_plays - 3))

            reward = sso.gameScore - self.gameScore_previous

            if sso.gameTick < 2000:
                if sso.gameWinner == 'PLAYER_LOSES':
                    reward += self.loss_reward
                elif sso.gameWinner == 'PLAYER_WINS':
                    reward += self.win_reward

            q_previous = self.w[0].dot(self.features_extended_previous)

            delta = reward - q_previous

            #print np.min(self.w[0]), np.max(self.w[0])
            #print 'END GAME:',reward, delta, q_previous

            self.e = self.Gamma * self.Lambda * self.e + \
                     (1.0 - self.Alpha * self.Gamma * self.Lambda * (self.e.dot(self.features_extended_previous))) * \
                     self.features_extended_previous

            self.w[0] += self.Alpha * (delta + q_previous - self.q_old) * self.e - \
                      self.Alpha * (q_previous - self.q_old) * self.features_extended_previous

            np.clip(self.w[0], -10.0, 10.0, out=self.w[0])

            if random.randint(0, 1) == 1:
                self.w[0], self.w[1] = self.w[1], self.w[0]

            # ==========================================================================================================

            if sso.gameWinner == 'PLAYER_LOSES':
                self.n_losses += 1
                #print ' LOSS |',sso.gameScore,'|',int(self.average_score),'| (',self.n_wins,'-',self.n_losses,')'

            elif sso.gameWinner == 'PLAYER_WINS':
                self.n_wins += 1
                #print '> WIN |',sso.gameScore,'|',int(self.average_score),'| (',self.n_wins,'-',self.n_losses,')'

        if self.n_plays == 3:
            self.generateDictionary()
            self.max_distance = self.max_dimensions[0] + self.max_dimensions[1]
            self.RUNNING_MODE = 1

        return (self.n_plays)%3

########################################################################################################################