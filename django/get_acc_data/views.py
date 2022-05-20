import json

from django.contrib import messages
from django.core.files.storage import FileSystemStorage
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render
from django.urls import reverse
from django.views.decorators.csrf import ensure_csrf_cookie, csrf_exempt
from django import forms


@csrf_exempt
def index(request):
    response_data = {}
    if request.method == 'POST':
        if request.FILES['acc_data']:
            acc_data_file = request.FILES['acc_data']
            fs = FileSystemStorage(location='acc_data_dir')
            filename = fs.save(acc_data_file.name, acc_data_file)
            response_data['filename'] = filename
    response_data['status'] = 'ok'
    return HttpResponse(status=204)
