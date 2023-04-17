#!/bin/sh
#SBATCH --job-name=python-example
#SBATCH --output=%x.o%j
#SBATCH --partition=cpu
#SBATCH --nodes=1     # nodes requested
#SBATCH --ntasks=1
#SBATCH --mem=10G
#SBATCH --time=0-01:00:00
#SBATCH --account=g_sowide

python3 count.py
