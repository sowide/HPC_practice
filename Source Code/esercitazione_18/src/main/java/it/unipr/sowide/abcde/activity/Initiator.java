package it.unipr.sowide.abcde.activity;

import java.util.Scanner;

import it.unipr.sowide.actodes.actor.passive.CycleListActor.TimeoutMeasure;
import it.unipr.sowide.actodes.configuration.Configuration;
import it.unipr.sowide.actodes.controller.SpaceInfo;
import it.unipr.sowide.actodes.distribution.activemq.ActiveMqConnector;
import it.unipr.sowide.actodes.executor.passive.CycleScheduler;
import it.unipr.sowide.actodes.service.creation.Creator;
import it.unipr.sowide.actodes.service.logging.ConsoleWriter;
import it.unipr.sowide.actodes.service.logging.Logger;
import it.unipr.sowide.actodes.util.logging.NoCycleProcessing;

/**
 * @author Mattia Pellegrino
 * @author Agostino Poggi
 * <br>
 * The {@code  Initiator} class starts a simulation
 * of the interaction among a set of persons.
 *
**/

@SuppressWarnings("unused")
public final class Initiator
{
  /**
   * The path of the default configuration file.
   *
  **/
  public static final String DEFAULTFILE =
      "./data/configuration/activity.properties";

  private Initiator()
  {
  }

  /**
   * Starts an actor space running the simulation
   * of the interaction among a set of persons.
   *
   *
   * @param v
   *
   * it requires two arguments: the "-cfg" option and
   * the absolute path of the file containing the configuration properties.
   *
   * If there are not arguments, then it uses the default configuration file.
   *
  **/
  public static void main(final String[] v)
  {
    
    Configuration c = SpaceInfo.INFO.getConfiguration();

      c.load(new String[] {"-cfg", DEFAULTFILE});
   
      String s,s2 = "";
      
      //se non ci sono argomenti da riga di comando allora 
      //il programma prende questi argomenti da console   
      if(v.length == 0)
      {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter:");
        System.out.println(" b for starting the broker of the application");
        System.out.println(" n for starting a node of the application");
        System.out.println(" i for starting the initiator of the application");
        System.out.println(" any other character for a standalone execution");      
        
        s = scanner.next();
        
        switch(s)
        {
          case "b":
          case "n":
          case "i":{
            System.out.println("Enter Broker IP Address and port");
            
            s2 = scanner.next();

            scanner.close();
          }
        }
        
        
      }
      else {
        s = v[0];
        s2 = v[1];
      }
      
      //Decommentare per abilitare il debug built-in di ActoDeS
      //c.setFilter(Logger.ACTIONS);
      //c.setLogFilter(new NoCycleProcessing());
      //c.addWriter(new ConsoleWriter());
      

    switch (s)
    {
      //lancio del broker
      case "b":
        c.setExecutor(new CycleScheduler(TimeoutMeasure.CY));
        c.setConnector(new ActiveMqConnector(s2, "")); // --> c.setConnector(new ActiveMqConnector(true));
        c.addService(new Creator());
        break;
      //lancio di un generico nodo
      case "n":
        c.setExecutor(new CycleScheduler(TimeoutMeasure.CY));
        c.setConnector(new ActiveMqConnector(s2)); // --> c.setConnector(new ActiveMqConnector(false));
        c.addService(new Creator());
        break;
      //lancio dell'initiator
      case "i":
        c.setExecutor(new CycleScheduler(
            new PersonManager(true), TimeoutMeasure.CY));
        c.setConnector(new ActiveMqConnector(s2)); // --> c.setConnector(new ActiveMqConnector(false));
        break;
      default:
        c.setExecutor(new CycleScheduler(
            new PersonManager(true), TimeoutMeasure.CY));
    }

    c.start();
  }
}