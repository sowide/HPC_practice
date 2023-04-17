import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from tensorflow.keras import models
from tensorflow.keras import layers


#Load dataset
dataset=pd.read_csv('cars.csv')
X=dataset.iloc[:,0:5].values
y=dataset.iloc[:,5].values
print("X:")
print(X)
print("\ny:")
print(y)


# Split the data in training set and test set
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.2)


# Print the shapes of the new X objects
print("\nTraining set dimensions (X_train):")
print(X_train.shape)
print("\nTest set dimensions (X_test):")
print(X_test.shape)

# Print the shapes of the new y objects
print("\nTraining set dimensions (y_train):")
print(y_train.shape)
print("\nTest set dimensions (y_test):")
print(y_test.shape)


# TODO: Create the model
model = models.Sequential()
model.add(layers.Dense(12, activation='relu',input_shape=(X_train.shape[1],)))
model.add(layers.Dense(8, activation='relu'))
model.add(layers.Dense(1,activation='linear'))
model.compile(optimizer='rmsprop', loss='mse', metrics=['mse'])

history = model.fit(X_train, y_train, epochs=150, batch_size=50, validation_split=0.2, verbose=1)



# TODO: Evaluate model

test_loss_score, test_mse_score = model.evaluate(X_test, y_test)
print("MSE: "+str(test_mse_score))
import math
print("RMSE: "+str(math.sqrt(test_mse_score)))