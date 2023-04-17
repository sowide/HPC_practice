package it.unipr.sowide.abcde.model;

/**
 *
 * The {@code Partition} class identifies the entities
 * managed by an actor space involved in the simulation.
 *
**/

public class Partition implements Unit
{
  private static final long serialVersionUID = 1L;

  private int min;
  private int max;

  /**
   * Class constructor.
   *
   * @param f  the number identifying the first entity of the partition.
   * @param n  the number identifying the first entity of the nest partition.
   */
  public Partition(final int f, final int n)
  {
    this.min = f;
    this.max = n;
  }

  /** {@inheritDoc} **/
  @Override
  public boolean isInUnit(final int i)
  {
    return (i >= this.min && i < this.max);
  }

  /** {@inheritDoc} **/
  @Override
  public String getName()
  {
    return this.min + "-" + this.max;
  }

  /** {@inheritDoc} **/
  @Override
  public int getIdMin()
  {
    return this.min;
  }

  /** {@inheritDoc} **/
  @Override
  public int getIdMax()
  {
    return this.max;
  }

  /** {@inheritDoc} **/
  @Override
  public int getPopulation()
  {
    return this.max - this.min;
  }
}
