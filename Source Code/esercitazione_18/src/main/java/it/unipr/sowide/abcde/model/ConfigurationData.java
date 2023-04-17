package it.unipr.sowide.abcde.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import it.unipr.sowide.actodes.registry.Reference;

/**
 *
 * The {@code ConfigurationData} class defines the data sent by the initial
 * manager to the other managers for the configuration of the simulation.
 *
**/

public class ConfigurationData implements Serializable
{
  private static final long serialVersionUID = 1L;

  private final int population;
  private final Map<Reference, Partition> partitionUnits;

  /**
   * Class constructor.
   *
   * @param pn  the number of entities involved in the simulation.
   * @param su  the set of social units of the simulation.
   * @param pu  the set of partition units of the simulation.
   */
  public ConfigurationData(final int pn, final Map<Reference, Partition> pu)
  {
    this.population     = pn;
    this.partitionUnits = pu;
  }

  /**
   * Gets the number of entities involved in the simulation.
   *
   * @return number of entities.
   *
  **/
  public int getPopulation()
  {
    return this.population;
  }

  /**
   * Gets the manager reference - partition unit pairs of the simulation.
   *
   * @return the manager reference - partition unit pairs.
   *
  **/
  public Map<Reference, Partition> getPartitionUnits()
  {
    return this.partitionUnits;
  }
}
