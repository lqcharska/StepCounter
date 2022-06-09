import os

from django.shortcuts import render
from statistics import mean
import csv
import numpy
from .forms import SelectedFile

from step_counter.preprocessing import magnituda, filtering, find_peak


directory_data_path = os.getcwd() + '\\acc_data_dir\\'


def index(request):
    files_list = arr = os.listdir(directory_data_path)
    print(files_list)
    list_time = []
    list_x = []
    list_y = []
    list_z = []
    all_data = []
    filename = ''
    file_path = ''
    mean_x = 0
    mean_y = 0
    mean_z = 0
    odchylenie_x = 0
    odchylenie_y = 0
    odchylenie_z = 0

    # if this is a POST request we need to process the form data
    if request.method == 'POST':
        # create a form instance and populate it with data from the request:
        form = SelectedFile(request.POST)
        # check whether it's valid:
        if form.is_valid():
            file_path = directory_data_path + form.cleaned_data['selected_file']
            filename = form.cleaned_data['selected_file']
            print("Valid form")

    if file_path == '':
        file_path = directory_data_path + files_list[0]

    # To bym zostawiła, bo w jednym projekcie było
    # if request.method == 'POST':
    #     if request.FILES['myfile']:
    #         myfile = request.FILES['myfile']
    #         fs = FileSystemStorage()
    #         filename = fs.save(myfile.name, myfile)

    with open(file_path, newline='') as csvfile:
        reader = csv.DictReader(csvfile, delimiter=';')
        print(reader)
        for row in reader:
            list_time.append(row['time'])
            list_x.append(float(row['x'].replace(',', '.')))
            list_y.append(float(row['y'].replace(',', '.')))
            list_z.append(float(row['z'].replace(',', '.')))
            all_data.append([row['time'], row['x'], row['y'], row['z']])

        mean_x = mean(list_x)
        mean_y = mean(list_y)
        mean_z = mean(list_z)

        # odchylenie standardowe czyli pierwiastek z wariancji
        odchylenie_x = numpy.std(list_x, ddof=1)
        odchylenie_y = numpy.std(list_y, ddof=1)
        odchylenie_z = numpy.std(list_z, ddof=1)


        arr_x = numpy.array(list_x)
        arr_y = numpy.array(list_y)
        arr_z = numpy.array(list_z)
        dane = numpy.vstack((arr_x,arr_y,arr_z))
        dane = dane.T
        magnitude = magnituda(dane)
        dane_po_filtracji = filtering(magnitude,12,500,4)
        peaks = find_peak(dane_po_filtracji)
        liczba_krokow = len(peaks)

    context = {
        'dataset_time': list_time,
        'dataset_x': list_x,
        'dataset_y': list_y,
        'dataset_z': list_z,
        'mean_x': mean_x,
        'mean_y': mean_y,
        'mean_z': mean_z,
        'odchylenie_x': odchylenie_x,
        'odchylenie_y': odchylenie_y,
        'odchylenie_z': odchylenie_z,
        'dataset_all': all_data,
        'filename': filename,
        'files_list': files_list,
        'step_count': liczba_krokow
    }

    return render(request, 'show_data/index.html', context)
