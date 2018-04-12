from scipy import signal
from preprecessing import  *

from keras.models import Model, load_model, Sequential
from keras.layers import Dense, Activation, Dropout, Input, Masking, TimeDistributed, LSTM, Conv1D
from keras.layers import GRU, Bidirectional, BatchNormalization, Reshape
from keras.optimizers import Adam
from sklearn.metrics import f1_score


def model(input_shape):
    """
    Function creating the model's graph in Keras.

    Argument:
    input_shape -- shape of the model's input data (using Keras conventions)

    Returns:
    model -- Keras model instance
    """

    X_input = Input(shape=input_shape)
    ### START CODE HERE ###
    batchNormalizer = BatchNormalization()
    droper = Dropout(0.8)
    # Step 1: CONV layer (≈4 lines)
    X = Conv1D(filters=196, kernel_size=15, strides=4, input_shape=input_shape)(X_input)  # CONV1D
    X = batchNormalizer(X)  # Batch normalization
    X = Activation('relu')(X)  # ReLu activation
    X = droper(X)  # dropout (use 0.8)
    # Step 2: First GRU Layer (≈4 lines)
    X = GRU(units=128, return_sequences=True)(X)  # GRU (use 128 units and return the sequences)
    X = droper(X)  # dropout (use 0.8)
    X = BatchNormalization()(X)  # Batch normalization

    # Step 3: Second GRU Layer (≈4 lines)
    X = GRU(units=128, return_sequences=True)(X)  # GRU (use 128 units and return the sequences)
    X = droper(X)  # dropout (use 0.8)
    X = BatchNormalization()(X)  # Batch normalization
    X = droper(X)  # dropout (use 0.8)

    # Step 4: Time-distributed dense layer (≈1 line)
    X = TimeDistributed(Dense(1, activation="sigmoid"))(X)  # time distributed  (sigmoid)

    ### END CODE HERE ###

    model = Model(inputs=X_input, outputs=X)

    return model


# model = model(input_shape = (Tx, n_freq))
# model.summary()