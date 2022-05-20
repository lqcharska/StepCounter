from django import forms


class SelectedFile(forms.Form):
    selected_file = forms.CharField(label='selected_file', max_length=100)
