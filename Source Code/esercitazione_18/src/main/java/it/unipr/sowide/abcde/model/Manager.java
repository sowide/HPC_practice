package it.unipr.sowide.abcde.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;


import org.javatuples.Pair;

import it.unipr.sowide.abcde.activity.Person;
import it.unipr.sowide.actodes.actor.Behavior;
import it.unipr.sowide.actodes.actor.CaseFactory;
import it.unipr.sowide.actodes.actor.MessageHandler;
import it.unipr.sowide.actodes.actor.MessagePattern;
import it.unipr.sowide.actodes.configuration.Configuration;
import it.unipr.sowide.actodes.controller.SpaceInfo;
import it.unipr.sowide.actodes.filtering.constraint.IsInstance;
import it.unipr.sowide.actodes.interaction.Create;
import it.unipr.sowide.actodes.interaction.Done;
import it.unipr.sowide.actodes.registry.Reference;

/**
 *
 * The {@code Manager} abstract class provides a partial implementation
 * of a behavior that manages the simulation of the entities
 * of the local partition unit.
 *
**/

public abstract class Manager<E extends Entity> extends Behavior
{
  private static final Long serialVersionUID = 1L;

  // Multiplier to modify the number of entities involved in the dimulation.
  private static final int MULTIPLIER;

  /**
   * The number of entities involved in the simulation.
   *
  **/
  public static final int POPULATION;


  protected static final int LENGTH;
  private static final Integer INITIALSTEP;
  private static final List<String> EPOQUES;

  /**
   * The simulation measures printing format.
   *
  **/
  public static final String MEASURESFORMAT;
  
  /**
   * Hashmap che contiene le informazioni degli infettinell'epoca corrente
   */
  protected HashMap<Integer, Integer> infectedInEpoque; 
  
  /**
   * Hashmap che tiene conto, per ogni epoca, il numero di persone infettate e agenti effettivamente infettanti
   */
  protected HashMap<Integer, Pair<Integer,Integer>> reportInfectedInEpoque; 
  
  

  /**
   * The random numbers generator.
   *
  **/
  public static final Random RANDOM = new Random();
  
  /**
   * booleano che serve ad abilitare o meno il salvataggio dei .csv delle epoche
   */
  public static final Boolean SAVEEPOQUE;
  /**
   * intero che specifica ogni quante epoche salvare un file .csv
   */
  public static final Integer SAVEEPOQUESTEPS;

  private static final int SLEEP = 100;

  static
  {
    Configuration c = SpaceInfo.INFO.getConfiguration();

    MULTIPLIER = c.getInt("person.multiplier");
    POPULATION = c.getInt("person.number") * MULTIPLIER;
    LENGTH     = c.getInt("simulation.length");
    
    SAVEEPOQUE = c.getBoolean("save.epoque");
    SAVEEPOQUESTEPS = c.getInt("save.epoque.steps");

    MEASURESFORMAT = c.getString("measures.format");
    FINALMEASURESINCSV = c.getBoolean("final_measures.in.csv");
    FINALMEASURESFORMAT = c.getString("final_measures.format");
    

    int i = c.getInt("initial.step");

    if (i <= 0)
    {
      i = 1;
    }

    INITIALSTEP = i;

    String[] l = c.getString("epoque.list").split(",");

    if (l.length == 1 && l[0].length() == 0)
    {
      EPOQUES = new ArrayList<String>();
    }
    else
    {
      EPOQUES = Arrays.asList(l);
    }
  }

  protected boolean isMaster;


  protected Map<Reference, Partition> partitionUnits;

  /**
   * The local partition unit.
   *
  **/
  protected Partition currentPartitionUnit;
  
  /**
  * lista che contiene tutte le informazioni per aggiornare i contatti
  * fra le epoche e simulare correttamente un'infezione
  */
 protected HashMap<Reference, List<Pair<Integer,Integer>>> contactsInEpoch; 

  /**
   * The entity ID - entity object pairs.
   *
  **/
  protected Map<Integer, E> entities;

  /**
   * The current number of simulation step.
   *
  **/
  protected Integer steps;

  /**
   * The simulation step - simulation measures pairs.
   *
  **/
  protected Map<Integer, String> measures;

  /**
   * The path of the root directory containing the simulation data.
   *
  **/
  protected Path directory;
  
  /**
   * Master's reference
   */
  protected Reference masterReference;
  
  /**
   * Hashmap che raccoglie i booleani delle varie partizione che comunicano di aver completato il proprio ciclo
   * questo è necessario per rendere effettiva la sincronizzazine
   */
  protected HashMap<Integer, List<Reference>> endCycle; 
  
  /**
   * booleano che diventa <code> True </code> quando tutte le partizioni hanno finito il proprio ciclo
   */
  public boolean synchronization = true; 
  
  /**
   * booleano utilizzato per mandare solo una volta i messaggi per non creare ridondanza nei cicli "dummy"
   */
  protected boolean sendOnce = true;
  
  /**
   * intero che specifica quanti cicli dummy fare per far si che la sincronizzazione sia effettiva
   */
  protected int extraCycles = 0;
  
  /**
   * Hashmap che raccoglie i booleani dalle varie partizioni che comuicano di aver raggiunto la lunghezza LENGTH
   * una volta collezionati tutti i booleani (che dovranno essere tutti "true") dalle altre partizioni
   * si avvia la procedura per terminare l'esecuzione
   */
  protected HashMap<Reference, Boolean> endCounter; 
  
  /**
   * HashMap che colleziona dalle altre partizioni i file delle misure che vengono generate
   * a fine corsa, unendole tutte insieme per formare un unico file di misure complessivo
   */
  protected HashMap<Reference, Map<Integer,String>> listMeasures;
  
  /**
  * Stringa che conterrà solamente la reference del Nodo 
  */
 protected String strReference;
 
 protected List<Integer> peopleinfected;


  private int min;

  /**
   * Class constructor.
   *
   * @param f
   *
   * this flag is <code>true</code>. if the manages has the duty of
   * creating the managers of the other actor spaces..
   *
  **/
  public Manager(final boolean f)
  {
    this.isMaster = f;
    this.contactsInEpoch = new HashMap<>();
    this.endCycle = new HashMap<>();
    this.endCounter = new HashMap<>();
    this.peopleinfected = new ArrayList<>();
    
    this.infectedInEpoque = new HashMap<>();
    this.reportInfectedInEpoque = new HashMap<>();

    
    if(this.isMaster) this.listMeasures = new HashMap<>();

    if (isMaster)
    {

      this.partitionUnits = new HashMap<>();

    }
    else
    {
      this.partitionUnits = null;
    }

    this.entities = new TreeMap<>();
    this.measures = new TreeMap<>();

    this.min = 0;

    this.steps = INITIALSTEP - 3;
  }

  /** {@inheritDoc} **/
  @Override
  public void cases(final CaseFactory c)
  {
    if (this.isMaster)
    {
      c.define(START, startHandler());
    }
    else
    {
      MessagePattern confData = MessagePattern.contentPattern(
          new IsInstance(ConfigurationData.class));

      c.define(confData, dataHandler());
    }
    
    //definito un nuovo messagge pattern per lo scambio di messaggi che contengono
    //le informazioni necessarie 
    MessagePattern messData = MessagePattern.contentPattern(
        new IsInstance(MessageData.class));
    c.define(messData, messageDataHandler());    

    c.define(CYCLE, cycleHandler());
  }

  /**
   * Builds the message handler that manages the data
   * for the configuration of social and partition units.
   *
   * @return the message handler.
   *
  **/
  public MessageHandler startHandler()
  {
    return (m) -> {
      int spaces = SpaceInfo.INFO.getProviders().size() + 1;

      int size = POPULATION / spaces;
      

      if (spaces > 1)
      {
        
        MessageHandler c1 = (n) -> {

          int max = this.min + size;
          
          this.partitionUnits.put((Reference) n.getContent(),
              buildPartitionUnit(this.min, max));

          this.min = max;
          

          if (this.partitionUnits.size() == spaces - 1)
          {
            this.currentPartitionUnit = buildPartitionUnit(
                this.min, POPULATION);

            this.partitionUnits.put(getReference(), this.currentPartitionUnit);

            ConfigurationData d = new ConfigurationData(POPULATION, this.partitionUnits);

            this.partitionUnits.keySet().forEach(e -> {
              if (!e.equals(getReference()))
              {
                send(e, d);
              }
            });

            this.directory = getDirectory();

            try
            {
              Files.createDirectories(this.directory);
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
            

            if (INITIALSTEP > 1)
            {
              loadPopulation();
            }
            else
            {
              buildPopulation();
            }
          }

          return null;
        };

        SpaceInfo.INFO.getProviders().forEach(e -> {

          Create o = new Create(buildManager());

          future(e, o, c1);
        });
      }
      else
      {
        this.currentPartitionUnit = buildPartitionUnit(this.min, POPULATION);

        this.partitionUnits.put(getReference(), this.currentPartitionUnit);

        this.directory = getDirectory();

        try
        {
          Files.createDirectories(this.directory);
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }

        if (INITIALSTEP > 1)
        {
          loadPopulation();
        }
        else
        {
          buildPopulation();
        }
      }

      return null;
    };
  }

  /**
   * Builds the message handler that manages the data
   * for the configuration of social and partition units.
   *
   * @return the message handler.
   *
  **/
  @SuppressWarnings("unchecked")
  public MessageHandler dataHandler()
  {
    return (m) -> {
      this.masterReference = m.getSender();
      
      ConfigurationData se = (ConfigurationData) m.getContent();

      this.partitionUnits = se.getPartitionUnits();

      this.currentPartitionUnit = this.partitionUnits.get(getReference());

      this.directory = getDirectory();

      try
      {
        Files.createDirectories(this.directory);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }

      if (INITIALSTEP > 1)
      {
        loadPopulation();
      }
      else
      {
        buildPopulation();
      }

      return null;
    };
  }
  
  
  /**
   * Builds the message handler that manages the data
   * for the configuration of the epidemic diffusion.
   *
   * @return the message handler.
   *
  **/
  public MessageHandler messageDataHandler() {
    
    return(m) -> {
       
      this.extraCycles = 2; //si impostano gli extra cicli dummy da fare a 2
      
      MessageData message = (MessageData) m.getContent();
      
      if (message.getContactsInEpoch() != null)
      {
        
        /**
         * TODO: Dichiarare una funzione che gestisca l'infezione aggiungendo informazioni
         * a questo messaggio
         *
         */
        
        
        //ricaviamo la lista di Triplet dal messaggio che ci è appena arrivato
        List<Pair<Integer, Integer>> listTriplet = message
            .getContactsInEpoch();
        
        //per ogni triplet nella lista eseguiamo sempre la stessa operazione
        listTriplet.forEach(pair -> {
          
          //il primo elemento della triplet contene una  tupla dell'id che deve esser aggiunto nella lista di contatti
          //dell'id destinatario
          int id = (int) pair.getValue0();
          int id_to_add = (int) pair.getValue1();

          Person p = (Person) this.entities.get(id);


          p.addSpecificContact(id_to_add); //metodo che aggiunge alla lista di contatti dell'individuo un id specifico
          

        });
      }
      
      if(message.getInfectionInEpoque() != null)
      {
        //si raccolgono le informazioni delle altre partizioni riguardo gli infettati e gli agenti infettanti nell'epoca corrente
        //successivamente si popola l'hashmap che tiene conto, epoca per epoca, di questi numeri
        HashMap<Integer, Integer> map = message.getInfectionInEpoque();
        map.keySet().forEach(key -> {
          if(this.infectedInEpoque.containsKey(key))
          {
            int num = this.infectedInEpoque.get(key) + map.get(key);
            this.infectedInEpoque.replace(key, num);
          }
          else this.infectedInEpoque.put(key, map.get(key));
            
        });
      }


      if (message.getEnd())
      {
        //viene popolata la lista endCounter che tiene il conto di quante partizioni abbiano finito il proprio ciclo
        this.endCounter.putIfAbsent(m.getSender(), message.getEnd());
      }

      

      if (message.getMeasure() != null && this.isMaster)
      {
        //vengono raccolte le misure finali di ogni partizoni
        this.listMeasures.putIfAbsent(m.getSender(), message.getMeasure());

        //quando vengono raccolte tutte allora viene invocato il metodo per generare il file complessivo di misure
        if ((this.listMeasures.size() == (this.partitionUnits.size() - 1)) && INITIALSTEP == 1) {
          saveFinalMeasures(); 
        }

      }

      
      if (message.getEndCycle() != null)
      {

        // a fine elaborazione del messaggio si popola l'hashmap che tiene conto
        // degli interi
        // che inidicano se una partizione ha finito o meno il suo ciclo

        if (!this.endCycle.containsKey(message.getEndCycle()))
        {
          List<Reference> l = new ArrayList<>();
          l.add(m.getSender());
          this.endCycle.put(message.getEndCycle(), l);
        }
        else
          this.endCycle.get(message.getEndCycle()).add(m.getSender());
      }

//      System.out.println(
//          "Ricevuto messaggio di fine epoca dal nodo: " + m.getSender());

      return null;
    };
  }

  /**
   * Builds the message handler that performs a simulation step.
   *
   * @return the message handler.
   *
  **/
  public MessageHandler cycleHandler()
  {
    return (m) -> {
      
      if (this.steps >= INITIALSTEP && this.steps <= LENGTH)
      {
        
        if(this.sendOnce) {
          
          this.sendOnce = false;
          communicate(); //metodo che raccoglie i dati necessari e li invia alle altre partizioni
        }

          
          //questo contatore tiene il conto delle partizioni che hanno terminato il proprio ciclo
          //riguardante l'epoca this.steps
          int endcount = 0;  
          
          
          
          if(this.endCycle.get((int) this.steps) != null) 
            {
            endcount = this.endCycle.get((int) this.steps).size();
            //System.out.println("Numero di messaggi ricevuti: " + this.endCycle.get((int) this.steps).size());
            }
          
          

          //se la variabile di sincronizzazione risulta vera, allora si può procedere al ciclo successivo
          //per verificare che sia vere si controlla che tutte le altre partizioni abbiano finito tutte le operazioni nel ciclo attuale
          ///(valore corrispondente in endCycle pari a "true"), si verifica anche che tutte le partizioni abbiano mandato questo tipo di messaggio
          //e in ultima istanza si verifica che son stati eseguiti i cicli dummy specificati dalla variabile extraCycles
          this.synchronization = (endcount == (this.partitionUnits.size() - 1))
              && (this.extraCycles == 0);
          

          //decremento della variabile extraCycles
          if (this.extraCycles > 0)
            this.extraCycles--;
          
        }
      
      if (this.steps >= LENGTH)
      {
        //quando si supera o si eguaglia la variabile LENGTH (giorni da simulare)
        //si verifica che tutte le altre partizioni siano arrivate a fine simulazioneù
        //quando si verificano queste ipotesi viene fatto terminare l'executor di ogni partizione
        if (this.endCounter.values().size() == (this.partitionUnits.size() - 1))
        {
          Configuration c = SpaceInfo.INFO.getConfiguration();

          c.getExecutor().shutdown();
        }
      }
      
      
      if (this.currentPartitionUnit != null && this.synchronization)
      {
        
        this.endCycle.remove(this.steps); //si rimuovo le informazioni non necessarie dall'hashMap endCycle
        
        
        if(this.isMaster && this.steps >= INITIALSTEP) //se la partizione attuale è quella del master e l'indice del ciclo attuale è maggiore rispetto a quello dello step iniziale
        {
          //Si calcolano le persone infettate in questa singola epoca
          if(this.infectedInEpoque != null)
          {
            int action = this.infectedInEpoque.keySet().size();
            int reaction = 0;
            
            for(@SuppressWarnings("rawtypes") Entry entry : this.infectedInEpoque.entrySet())
            {
              reaction += this.infectedInEpoque.get(entry.getKey());
            }
            
            this.reportInfectedInEpoque.put(this.steps, new Pair<Integer, Integer>(action, reaction));
            this.infectedInEpoque.clear();
          }
          else
          {
            this.reportInfectedInEpoque.put(this.steps, new Pair<Integer,Integer>(0,0));
          }
        }

        
        this.steps++;

        if (((EPOQUES.size() == 0
            || EPOQUES.contains(Integer.toString(this.steps)))
            && (this.steps >= INITIALSTEP) && (this.steps <= LENGTH)))
        {
         
         //si verifica se si debbano salvare le epoche
         //se si, viene anche specificata la cadenza di salvataggio
         if(SAVEEPOQUE && (this.steps % SAVEEPOQUESTEPS == 0)) saveEpoque();
         
         saveMeasure();
         
        }

        if (this.steps == LENGTH)
        {
          saveMeasures();

        }
        else if (this.steps > LENGTH)
        {
          try
          {
            Thread.sleep(SLEEP);
          }
          catch (Exception e)
          {
          }
        }
        
        this.sendOnce = true;
      }

      return null;
    };
  }

  /**
   * Gets the partition units involved in the simulation.
   *
   * @return the partition units.
   *
  **/
  public Collection<Partition> getPartitionUnits()
  {
    return this.partitionUnits.values();
  }

  /**
   *  Builds a concrete manager.
   *
   * @return the manager.
   *
  **/
  protected abstract Manager<E> buildManager();




  /**
   *  Builds a partition unit.
   *
   * @param f  the number identifying the first entity of the unit.
   * @param n  the number identifying the first entity of the next unit.
   *
   * @return the partition unit.
   *
  **/
  protected abstract Partition buildPartitionUnit(int f, int n);

  /**
   *  Builds the population of a partition unit..
   *
  **/
  protected abstract void buildPopulation();

  /**
   *  Loads the population of a partition unit.
   *
  **/
  protected abstract  void loadPopulation();

  /**
   * Gets the path of the root directory containing the simulation data.
   *
   * @return the path.
   *
  **/
  protected abstract Path getDirectory();

  /**
   * Gets the current epoque file path.
   *
   * @param e  the epoque number.
   *
   * @return the name.
   *
  **/
  protected abstract String getEpoqueFilePath(Integer e);

  /**
   * Gets the measures file path.
   *
   * @return the name.
   *
  **/
  protected abstract String getMeasuresFilePath();
  
  /**
   * The simulation final measures printing format
   */
  public static final String FINALMEASURESFORMAT;
  
  /**
   * Se settato a <code> True </code> abilita il salvataggio delle misure in formato csv
   */
  public static final Boolean FINALMEASURESINCSV;
  
  /**
   * Saves the final measures file
   */
  protected abstract void saveFinalMeasures();

  /**
   * Saves the current epoque file.
   *
  **/
  protected abstract void saveEpoque();
  
  /**
   * Funziona che viene richiamata a fine ciclo quando si devono comunicare le informazioni alle altre partizioni
   */
  protected abstract void communicate();

  /**
   * Saves the current measure.
   *
  **/
  protected abstract void saveMeasure();

  /**
   * Saves the measures file.
   *
  **/
  protected abstract void saveMeasures();
}
