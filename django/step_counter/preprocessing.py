# Functions to preprocess data
import numpy as np
import scipy
from scipy.signal import filtfilt, butter, find_peaks
import math


def magnituda(data):
    magnituda = np.zeros(np.shape(data)[0])
    for i in range(np.shape(data)[0]):
        magnituda[i] = np.sqrt(np.power(data[i, 0], 2) + np.power(data[i, 1], 2) + np.power(data[i, 2], 2))
    return magnituda


def filtering(data, f_cut_off, fs, order=4):
    filtered_data = np.zeros((np.shape(data)[0]))
    b, a = scipy.signal.butter(order, f_cut_off, btype='low', analog=False, fs=fs)
    filtered_data = scipy.signal.filtfilt(b, a, data)
    return filtered_data


def find_peak(data):
    peaks, _ = find_peaks(data)
    return peaks


def moving_variance(signal, window):
    mean_of_window = np.zeros(len(signal))
    variance = np.zeros(len(signal))
    sd = np.zeros(len(signal))
    for i in range(window, len(signal)):
        mean_of_window[i - window] = np.sum(signal[i - window:i]) / window
        variance[i - window] = np.sum(np.power(signal[i - window:i] - mean_of_window[i - window], 2)) / window
        sd[i - window] = np.sqrt(np.sum(np.power(signal[i - window:i] - mean_of_window[i - window], 2)) / window)
    return mean_of_window, variance, sd


def cut_non_gait_data(signal, window):
    for i in range(window, len(signal)):
        if all(elem < 0 for elem in signal[i - window:i]):
            begin = i
            print(begin)
            break
    for j in range(begin, window * int(math.ceil(len(signal) / window))):
        if all(elem > 0 for elem in signal[j - window:j]):
            end = j - window
            print(end)
            break
    signal_after_cut = signal[begin:end]
    return signal_after_cut
