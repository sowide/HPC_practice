#!/usr/bin/env bash

test -n "$SLURM_JOB_NAME" || exit

log="${SLURM_JOB_NAME}.o${SLURM_JOB_ID}.${OMPI_COMM_WORLD_RANK}.$(hostname -s).log"

echo "This is rank $OMPI_COMM_WORLD_RANK on $(hostname -s)." >> "$log"
echo "The master node is ${HOSTNAME}." >> "$log"
echo "Host $(host ${HOSTNAME})." >> "$log"

sleep $((2*(OMPI_COMM_WORLD_RANK+1)))

echo "OK from rank $OMPI_COMM_WORLD_RANK"

# Run 1 job per task
N_JOB=$OMPI_COMM_WORLD_RANK                # create as many jobs as tasks
N_TASK=$OMPI_COMM_WORLD_LOCAL_SIZE
N_JOB_MAX=$(($OMPI_COMM_WORLD_SIZE - 1))

#echo $N_JOB
#echo $N_TASK
#echo $N_JOB_MAX

if [ $N_JOB -gt 0 ]
	then
		echo "Need to Sleep $((5 * $N_JOB)) sec"
		sleep $((5 * $N_JOB))
fi

if [ $N_JOB -eq 0 ] || [ $N_JOB -eq $N_JOB_MAX ]
  then
      if [ $N_JOB -eq 0 ]
         then
	   echo "Lancio del Broker"
           java -jar MultiActorSpaces.jar b tcp://${HOSTNAME}:61616 >> "$log"
         else
	   echo "Lancio dell'Initiator"
           java -jar MultiActorSpaces.jar i tcp://${HOSTNAME}:61616 >> "$log"
      fi
else
	   java   -jar MultiActorSpaces.jar n tcp://${HOSTNAME}:61616 >> "$log"
  fi
