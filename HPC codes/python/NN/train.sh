#!/bin/bash

#SBATCH --partition=cpu
#SBATCH --output=%x.o%j
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
##SBATCH --gres=gpu:a100:1
#SBATCH --time 0-01:00:00
#SBATCH --mem=1G

echo $SLURM_JOB_NODELIST

echo  #OMP_NUM_THREADS : $OMP_NUM_THREADS

#source /hpc/share/tools/python/3.6.3/virtualenv/gpu/machine-learning/bin/activate

module load miniconda3
source "$CONDA_PREFIX/etc/profile.d/conda.sh"
conda activate machine-learning-cuda-10.2 
#pip install einops

pip install transformers
pip list

python cars_regression.py

conda deactivate
