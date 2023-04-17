package it.unipr.sowide.abcde.model;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.javatuples.Pair;



/**
 * @author Mattia Pellegrino
 * @author Agostino Poggi
 * <br>
 * The {@code MessageData} class defines the data sent by the actor spaces
 * to the other actor spaces for the configuration of the simulation.
 * <br>
 * Questa classe viene utilizzata per inviare attraverso le partizioni, tutte le informazioni necessarie
 *
**/
public class MessageData implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  private List<Pair<Integer, Integer>> contactsInEpoch;
  private Map<Integer,String> measure;
  private Integer endCycle;
  private Boolean end;
  private HashMap<Integer, Integer> infectionInEpoque;
  
  //
  /**
   * Class Constructor.
   * 
   * @param contacts    lista di pair che contiene le informazioni necessarie per l'aggiornamento dei contatti
   * @param measure     tupla di hashmap che contiene le informazioni degli archi entranti ed entranti/uscenti dei vari nodi
   * @param end         Hashmap che contiene tutte le misure delle altre partizioni
   * @param infInEpoque HashMap che tiene conto degli infettati e degli agenti infettanti nell'epoca corrente
   */
  public MessageData(
      final List<Pair<Integer, Integer>> contacts,
      final Map<Integer, String> measure,
      final Integer steps,
      final Boolean end,
      final HashMap<Integer, Integer> infInEpoque)
  {

    this.contactsInEpoch = contacts;
    this.measure = measure;
    this.endCycle = steps;
    this.end = end;
    this.infectionInEpoque = infInEpoque;

  }


  /**
   * Get information about contacts
   * @return a triplet that containts the information about contacts' interaction
   */
  public List<Pair<Integer, Integer>> getContactsInEpoch()
  {
    return this.contactsInEpoch;
  }
  
  /**
   * Get measure information
   * @return the measure information about a specific partition
   */
  public Map<Integer, String> getMeasure()
  {
    return this.measure;
  }
  
  /**
   * Get the end cycle message
   * @return a integer that is used to determinated when a partition ends its periodical cycle
   */
  public Integer getEndCycle()
  {
    return this.endCycle;
  }

  /**
   * Get the end cycle message 
   * @return a boolean, if {@code True} the cycle has ended
   */
  public Boolean getEnd()
  {
    return this.end;
  }
  
  
  /**
   * Get information about the infection people in a specific epoch
   * @return a HashMap that contains information about people that are infected and people that have spread the virus
   */
  public HashMap<Integer, Integer> getInfectionInEpoque()
  {
    return this.infectionInEpoque;
  }

}