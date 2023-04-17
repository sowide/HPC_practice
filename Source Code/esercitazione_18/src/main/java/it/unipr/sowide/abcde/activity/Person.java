package it.unipr.sowide.abcde.activity;

import static it.unipr.sowide.abcde.model.Manager.RANDOM;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unipr.sowide.abcde.model.Entity;
import it.unipr.sowide.abcde.model.Manager;
import it.unipr.sowide.actodes.actor.CaseFactory;
import it.unipr.sowide.actodes.actor.MessageHandler;
import it.unipr.sowide.actodes.configuration.Configuration;
import it.unipr.sowide.actodes.controller.SpaceInfo;

import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * The {@code Person} class defines a behavior simulating a person
 * that interacts with the the other persons involved in the simulation.
 *
**/

public class Person extends Entity
{
  private static final long serialVersionUID = 1L;
  
  private static final double TRANSMISSION_PROB; //variabile che specifica la probabilità di infettare un altro attore
  //quando ci si trova nella fase di infezione

  static
  {
    Configuration c =  SpaceInfo.INFO.getConfiguration();
    
    TRANSMISSION_PROB     = c.getDouble("transmission.probability");

  }

  private Activity activity;
  private Set<Integer> contacts;

  /**
   * variabile che conterrà la fase del nostro attore
   */
  private Phases phase; 

  private PersonManager manager;
  
  private double transmission_probability; //variabile che conterrà la probabilità di infezione
 

  /**
   *
   * @param i  the person identifier.
   * @param n  the person province name.
   * @param a  the person interaction frequency.
   * @param m  the person manager.
   *
  **/
  public Person(final int i, final PersonManager m, final Phases p)
  {
    super(i);

    this.manager = m;
    //settaggio della variabile relativa alla probabilità di infezione
    this.transmission_probability = TRANSMISSION_PROB;
    
    this.phase    = p;

    this.contacts = new HashSet<Integer>();
  }

  /**
   *
   * @param i  the person identifier.
   * @param n  the person province name.
   * @param a  the person interaction frequency.
   * @param m  the person manager.
   * @param c  the initial list of contacts.
   *
  **/
  public Person(final int i,
      final PersonManager m, final List<String> c, final Phases p)
  {
    this(i, m, p);

    for (String s : c)
    {
      this.contacts.add(Integer.parseInt(s));
    }
  }

  /**
   * Performs a simulation step.
   *
   * @return <code>null</code>.
   *
  **/
  public MessageHandler cycleHandler()
  {
    return (m) -> {
      
      if (this.manager.synchronization) {
      manage();
      }

      return null;
    };
  }

  /** {@inheritDoc} **/
  @Override
  public void cases(final CaseFactory c)
  {
    c.define(START, cycleHandler());
    c.define(CYCLE, cycleHandler());
  }

  /**
   * Gets the person interaction frequency.
   *
   * @return the frequency.
   *
  **/
  public Activity getActivity()
  {
    return this.activity;
  }
  
  /**
   * Gets the person infection probability.
   *
   * @return the infection probability.
   *
  **/
  public double getTransmission_probability()
  {
    return this.transmission_probability;
  }
  
  /**
   * Gets the person current phase.
   *
   * @return the phase.
   *
  **/
  public Phases getPhase()
  {
    return this.phase;
  }
  
  /**
   * Sets the person phase to a specific value
   * 
   * @param phase
   */
  public void setPhase(Phases phase)
  {
    this.phase = phase;
  }

  /**
   * Gets the identifiers of the contacted persons.
   *
   * @return the identifiers.
   *
  **/
  public Set<Integer> getContacts()
  {
    return this.contacts;
  }

  /** {@inheritDoc} **/
  @Override
  public void manage()
  {
    addContact();
  }
  
  /**
   * Calculate the number of contacts to meet in the current epoch
   * 
   * @return the number of contacts
   */
  public Integer getNumberContactsInEpoch()
  {
    return  ThreadLocalRandom.current().nextInt(0, 3 + 1);
  }
  
  //metodo che aggiunge all lista di contatti uno specifico id
  public void addSpecificContact(final int id)
  {
    this.contacts.add(id);
  }
  
  /**
   * Adds a new contact to a person.
   */
  private void addContact()
  {
    
    
    if(this.getId() % 1000 == 0)
    {
      //System.out.println("Person: " + this.getId() + " contacts:" + this.contacts.size() );
    }
    
    int num_people = this.getNumberContactsInEpoch();
    
    for (int k=0; k < num_people; k++) {
    int i = this.getId();

    int j = Manager.RANDOM.nextInt(Manager.POPULATION);

    while (i == j && !this.contacts.contains(j))
    {
      j = Manager.RANDOM.nextInt(Manager.POPULATION);
    }
    
    this.contacts.add(j);
  
    this.manager.encounterHandlerInPartition(j, i);
    
    }
    
  }

 
  }




