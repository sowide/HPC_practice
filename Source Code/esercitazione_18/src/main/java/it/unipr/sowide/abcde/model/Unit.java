package it.unipr.sowide.abcde.model;

import java.io.Serializable;

/**
 *
 * The interface {@code Unit} identifies the units decomposes
 * in sets of groups the entities involved in a simulation.
 *
 * Note that each unit contains a set of entities identified
 * by a continuous sequence of numbers and that the entities of
 * the sequence of units involved in the simulation define
 * a continuous sequence of numbers.
 *
**/

public interface Unit extends Serializable
{
  boolean isInUnit(int i);

  /**
   * Gets the name of the unit.
   *
   * @return the name.
   *
  **/
  String getName();

  /**
   * Gets the number identifying the first entity of the unit.
   *
   * @return the number.
   *
  **/
  int getIdMin();

  /**
   * Gets the number identifying the first entity of the next unit.
   *
   * @return the number.
   *
  **/
  int getIdMax();

  /**
   * Gets the number of entities in the unit.
   *
   * @return the number of entities.
   *
  **/
  int getPopulation();
}
