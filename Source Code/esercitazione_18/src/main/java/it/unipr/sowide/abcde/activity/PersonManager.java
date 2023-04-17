package it.unipr.sowide.abcde.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.javatuples.Pair;


import it.unipr.sowide.abcde.model.Manager;
import it.unipr.sowide.abcde.model.MessageData;
import it.unipr.sowide.abcde.model.Partition;
import it.unipr.sowide.actodes.configuration.Configuration;
import it.unipr.sowide.actodes.controller.SpaceInfo;
import it.unipr.sowide.actodes.registry.Reference;

/**
 *
 * The {@code PersonManager} class defines a repository maintaining
 * the information about the interaction between a set of persons.
 *
**/

public class PersonManager extends Manager<Person>
{
  private static final long serialVersionUID = 1L;

  /**
   * Class constructor.
   *
   * @param f
   *
   * this flag is <code>true</code>. if the manages has the duty of
   * creating the managers of the other actor spaces..
   *
  **/
  public PersonManager(final boolean f)
  {
    super(f);
  }

  /** {@inheritDoc} **/
  @Override
  protected Path getDirectory()
  {
    Configuration c = SpaceInfo.INFO.getConfiguration();

    return Paths.get(c.getString("data.pathname"),
        new SimpleDateFormat("yyyy-MMM-dd'-'HHmmss").format(new Date()),
        this.currentPartitionUnit.getName()).toAbsolutePath().normalize();
  }

  /** {@inheritDoc} **/
  @Override
  public String getEpoqueFilePath(final Integer e)
  {
    if (e >= 0)
    {
      return this.directory + File.separator + "epoque" + e + ".csv";
    }

    return "";
  }

  /** {@inheritDoc} **/
  @Override
  public String getMeasuresFilePath()
  {
    return this.directory + File.separator + "measures.txt";
  }
  
  public String getFinalMeasuresFilePath()
  {
    return this.directory + File.separator + "finalMeasures.txt";
  }
  
  public String getCsvFinalMeasuresFilePath()
  {
    return this.directory + File.separator + "finalMeasures.csv";
  }



  /** {@inheritDoc} **/
  @Override
  protected Manager<Person> buildManager()
  {
    return new PersonManager(false);
  }



  /** {@inheritDoc} **/
  @Override
  protected Partition buildPartitionUnit(final int f, final int n)
  {
    return new Partition(f, n);
  }

  /** {@inheritDoc} **/
  @Override
  protected void buildPopulation()
  {
    this.strReference = this.getReference().toString().substring(this.getReference().toString().indexOf(".") + 1, this.getReference().toString().indexOf("@"));
    
    /**
     * TODO
     * Inizializza le persone in modo corretto
     **/
    Phases p = null;
    
    
    for (int i = this.currentPartitionUnit.getIdMin();
         i < this.currentPartitionUnit.getIdMax(); i++)
    {

      Person v = new Person(i, this,p);

      this.entities.put(i, v);

      actor(v);
    }
    
    setinfectedpeople();
  }

  /** {@inheritDoc} **/
  @Override
  protected void loadPopulation()
  {
    try
    {
      BufferedReader r = new BufferedReader(
          new FileReader(getEpoqueFilePath(this.steps + 1)));

      String l = r.readLine();

      while (l != null)
      {
        String[] s = l.split(",");
        String[] a = s[0].split(":");
        List<String> cl = Arrays.asList(s).subList(1, s.length);

        int i = Integer.parseInt(a[0]);
        Phases phase = (Phases) Phases.valueOf(a[3]);

        Person v = new Person(i, this, cl, phase);

        this.entities.put(i, v);

        actor(v);

        l = r.readLine();
      }

      r.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  /**
   * Funzione che scopre la partizione alla quale appartiene uno specifico id
   * @param i Id da ricercare
   * @return la partizione a cui appartine l'attore {@code i}
   */
  public Reference findPartition(final int i) {
    
    Reference ref = null;
    for( @SuppressWarnings("rawtypes") Map.Entry entry : this.partitionUnits.entrySet())
    {
      Partition p = (Partition) entry.getValue();
      if( i >= p.getIdMin() && i <= p.getIdMax() - 1)
      {
        ref = (Reference) entry.getKey();
        return ref;
      }
    }
    
    return null;
  }
  
  //metodo che gestisce l'incontro fra due attori 
  public void encounterHandlerInPartition(final int id,final int id_to_add) {
    
    Person pj = (Person) this.entities.get(id_to_add);
    
    //se le due persone appartengono alla stessa partizione allora il manager
    //notificherà all'altro attore che deve aggiornare la propria lista e farà
    //partire un metodo che si occuperà delle infenzioni che avvengono nella medesima partizione
    if(this.entities.containsKey(id)) 
    {
    Person pi = (Person) this.entities.get(id);
    
    pi.addSpecificContact(id_to_add);
    
    infectionInPartition(pi, pj);
    
    }
    //se invece la persona che è stata incontrata fa parte di un'altra partizione vengono preparate le informazioni da inviare
    else {
      Pair<Integer, Integer> pair = Pair.with(id, id_to_add); //tupla che contiene l'id e l'id da aggiungere al contatto specifico
      
      //viene popolata un'hashmap che organizza le informazioni da inviare catalogata a seconda della reference della partizione in questione
      //se l'hahsmap è vuota viene correttamente inizializzata 
       if (!this.contactsInEpoch.containsKey(findPartition(id)))
       {
         List<Pair<Integer, Integer>> list = new ArrayList<>();
         list.add(pair);
         this.contactsInEpoch.put(findPartition(id), list);
       }
       else
         this.contactsInEpoch.get(findPartition(id)).add(pair);
    }
      
    }
  
  /**
   * funzione che si occupa dell'invio dei dati che sono stati generati con la funzione {@code encounterHandlerInPartition}
   */
 protected void communicate()
  {
   
    System.out.println("Nodo: " + this.strReference + " Comunicazione inviata");
   //vengono prese tutte le reference nell'hashmap partitionUnits
    this.partitionUnits.keySet().forEach(c -> {
      
      //vengono popolate le varie variabili da inviare alle altre partizioni
      if(!c.equals(getReference()))
      {
        List<Pair<Integer, Integer>> contacts = null;
        Boolean end = false;
        Map<Integer, String> measure = null;
        HashMap<Integer,Integer> infInEpoque = null;
        
        if(this.contactsInEpoch.containsKey(c)) contacts = this.contactsInEpoch.get(c);
        
        if(c.equals(this.masterReference)) {
          infInEpoque = this.infectedInEpoque;
          
        }
        
        if(this.steps == LENGTH) end = true;
        
        if(this.steps == LENGTH && c.equals(this.masterReference)) measure = this.measures; 
        
        //invio dei dati alle altre partizioni
        send(c, new MessageData(contacts, measure, steps, end, infInEpoque));
        
      }
    });
    
    //alla fine vengono svuotate le hashmap per poter essere riutilizzate al ciclo successivo
    this.contactsInEpoch.clear();
    

  }
 
 /**
  *  funzione che simula l'infezione nel caso in cui una persona suscettibile
  * incontra un positivo
  * e viceversa
  */
 public void infectionInPartition(final Person pi, final Person pj)
 {
   double prob = RANDOM.nextDouble();
   double infectionProbability;
   
   

   //
  //
   /**
    * TODO 
    * Completare l'implementazione della seguente funzione
    * il primo step è verificare se una persona suscettibile ha incontrato una persona infetta o positiva
    * il secondo step è verificare se la trasmissione è avvenuta
    * 
    * Hints: utilizzare il metodo getTransmission_probability della classe Person
    *        per simulare l'infezione estrarre un numero float casuale compreso fra 0 e 1
    *        
    *
    */

 }
    

  /** {@inheritDoc} **/
  @Override
  protected void saveEpoque()
  {

    
    try (FileWriter s = new FileWriter(getEpoqueFilePath(this.steps)))
    {
      //System.out.println("salvo epoca: "+ this.steps);
      for (Person p : this.entities.values())
      {
        List<String> l = new ArrayList<>();

        for (Integer i : p.getContacts())
        {
          l.add(Integer.toString(i));
        }

        String pv = String.join(",", l);

        s.write(p.getId() + ":" + p.getContacts().size() + "," + pv + ":" + p.getPhase() + "\n");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  
  

protected void setinfectedpeople()
{
  /**
   * TODO
   * questa funzione deve inizializzare alcuni agenti direttamente ad infetti  
   * l'estrazione può essere fatta casualmente fra gli id delle persone che la partizione gestisce
   * 
   * Hints: this.currentPartitionUnit.getIdMax() e this.currentPartitionUnit.getIdMin() restituiscono gli estremi degli id delle persone 
   * gestite dalla partizione corrente
   * Usa this.entities.get per selezionare un agente presente nella partizione utilizzando il suo id univoco
   *
   */
  
}

  @SuppressWarnings("incomplete-switch")
  @Override
  protected void saveMeasure()
  {
  
    if(!this.isMaster) System.out.println("Nodo: " + strReference + " Epoca: " + this.steps);
    else {
      System.out.println("Epoca: " + this.steps);
    }
    List<Integer> values = new ArrayList<Integer>();

    long sum = 0;
    int min = -1;
    int max = 0;
    int infected = 0;
    
    for (Person p : this.entities.values())
    {
      values.add(p.getNumberContactsInEpoch());
      // a seconda della fase, viene incrementato il contatore corretto
      /**
       * TODO personalizzare il seguente switch
       */
      switch (p.getPhase())
      {
        case Nothing:
          infected++;
          break;
      }
    }

    for (Person p : this.entities.values())
    {
      values.add(p.getNumberContactsInEpoch());

    }

    Collections.sort(values, Collections.reverseOrder());
    
    sum = values.stream()
        .collect(Collectors.summingInt(Integer::intValue));
    
    max = values.stream().max(Comparator.comparing(Integer::valueOf))
        .get();
    
    min = values.stream().min(Comparator.comparing(Integer::valueOf))
        .get();

    String measures = (String.format("%.5f", (double) sum / values.size()))
        + ":" + sum + ":" + min + ":" + max + ":" + Integer.toString(infected) ;

    this.measures.put(this.steps, measures);
    
    //quando sono state salvati i dati delle misure allora vengono 
    //resettati i valori necessati a sbloccare il ciclo attivo successivo

  }

  /** {@inheritDoc} **/
  @Override
  protected void saveMeasures()
  {
    try (FileWriter w = new FileWriter(getMeasuresFilePath()))
    {
      for (Entry<Integer, String> me : this.measures.entrySet())
      {
        String[] ms = me.getValue().split(":");

        String line = String.format(MEASURESFORMAT,
            Integer.toString(me.getKey()), ms[0], ms[1], ms[2], ms[3]);

        w.write(line);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    System.out.println("measures saved");
  }
  
  //funzione che serve a generare un file di misure finale unico
  //prende i valori delle misure dalle altre partizioni e 
  //calcola i valori complessivi
  protected void saveFinalMeasures()
  {
    double[] var1 = new double[(int) LENGTH];
    long[] var2 = new long[(int) LENGTH];
    int[] var3 = new int[(int) LENGTH];
    Arrays.fill(var3, -1);
    int[] var4 = new int[(int) LENGTH];
    Arrays.fill(var4, -1);
    int[] var5 = new int[(int) LENGTH];
    
    

    for (Reference key : this.listMeasures.keySet())
    {

      for (int i = 0; i < this.listMeasures.get(key).size(); i++)
      {
        String[] ms = this.listMeasures.get(key).get((i + 1)).split(":");
        var1[i] += Double.parseDouble(ms[0]);
        var2[i] += Long.parseLong(ms[1]);
        if (var3[i] > Integer.parseInt(ms[2]) || var3[i] == -1) var3[i] = Integer.parseInt(ms[2]);
        if (var4[i] == -1 || var4[i] < Integer.parseInt(ms[3])) var4[i] = Integer.parseInt(ms[3]);
        var5[i] += Integer.parseInt(ms[4]);
      }

    }

    try (FileWriter w = new FileWriter(getFinalMeasuresFilePath()))
    {
      for (int i = 0; i < this.measures.size(); i++)
      {
        String[] ms = this.measures.get((i + 1))
            .split(":");

        var1[i] += Double.parseDouble(ms[0]);
        var2[i] += Long.parseLong(ms[1]);
        if (var3[i] > Integer.parseInt(ms[2]) || var3[i] == -1) var3[i] = Integer.parseInt(ms[2]);
        if (var4[i] == -1 || var4[i] < Integer.parseInt(ms[3])) var4[i] = Integer.parseInt(ms[3]);
        var5[i] += Integer.parseInt(ms[4]);


        //DecimalFormat df = new DecimalFormat("#.#");

      /**  String line = String.format(MEASURESFORMAT, Integer.toString(i + 1),
            (var1[i] / this.partitionUnits.size()), var2[i], var3[i], var4[i],
            ms[4], var5[i], df.format(var5[i] * 100 / var2[i]),
            ms[7], ms[8], var7[i],
            df.format(100 - (var5[i] * 100 / var2[i])), ms[11], var9[i],
            var10[i], var11[i], var12[i], var13[i], var14[i]); **/
        
        
        String line = String.format(FINALMEASURESFORMAT, Integer.toString(i + 1), (var1[i]/this.partitionUnits.size()),var2[i] ,var3[i],var4[i], var5[i] );

        w.write(line);
        
        
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    
    
    if (FINALMEASURESINCSV)
    {
      try (FileWriter w2 = new FileWriter(getCsvFinalMeasuresFilePath()))
      {
        String header = "EPOCH,AVERAGE,TOTAL,MIN,MAX,INFECTED\n";
        w2.write(header);

        for (int i = 0; i < this.measures.size(); i++)
        {
          String line = Integer.toString(i + 1) + "," + (var1[i]/this.partitionUnits.size()) + "," + var2[i]
              + "," + var3[i] + "," + var4[i] + "," + var5[i] + "\n";

          w2.write(line);
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    System.out.println("final measures saved");
  }
}
