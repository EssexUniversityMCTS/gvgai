package ontology.physics;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 12:05
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class NoFrictionPhysics extends ContinuousPhysics
{
    public NoFrictionPhysics()
    {
        super();
        friction = 0;
    }
}
