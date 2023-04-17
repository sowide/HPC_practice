#!/bin/bash
#SBATCH --job-name=test
#SBATCH --output=%x.o%j
#SBATCH --error=%x.e%j
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=5
#SBATCH --partition=cpu
#SBATCH --mem=150G
##SBATCH --nodelist=wn33
##SBATCH --mem-per-cpu=15G
#SBATCH --cpus-per-task=1
#SBATCH --time=0-02:00:00
#SBATCH --account=g_sowide


module load gnu openmpi
module load java/jdk/12.0.1

mpirun --mca btl_tcp_if_include ib0 ./test.sh
