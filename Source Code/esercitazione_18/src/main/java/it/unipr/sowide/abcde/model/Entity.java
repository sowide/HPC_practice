package it.unipr.sowide.abcde.model;

import it.unipr.sowide.actodes.actor.Behavior;
import it.unipr.sowide.actodes.actor.CaseFactory;
import it.unipr.sowide.actodes.actor.MessageHandler;
/**
 *
 * The {@code Entity} class defines a partial implementation of a behavior
 * simulating an entity that interacts with the the other entities involved
 * in the simulation.
 *
**/

public abstract class Entity extends Behavior
{
  private static final long serialVersionUID = 1L;

  private int id;

  /**
   *
   * @param i  the entity identifier.
   * @param u  the unit name.
   *
  **/
  public Entity(final int i)
  {
    this.id   = i;
  }


  /** {@inheritDoc} **/
  @Override
  public void cases(final CaseFactory c)
  {
    c.define(START, cycleHandler());
    c.define(CYCLE, cycleHandler());
  }

  /**
   * Gets the entity identifier.
   *
   * @return the identifier.
   *
  **/
  public int getId()
  {
    return this.id;
  }


  /**
   * Performs a simulation step.
   *
   * @return <code>null</code>
   *
  **/
  public MessageHandler cycleHandler()
  {
    return (m) -> {
      manage();

      return null;
    };
  }

  /**
   * Provides the code implementing the entity simulation step.
   *
  **/
  protected abstract void manage();
}
