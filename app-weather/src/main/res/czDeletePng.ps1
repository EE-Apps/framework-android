Get-ChildItem -Filter *.png -Recurse | Remove-Item -Force
Get-ChildItem -Filter *.jpg -Recurse | Remove-Item -Force
Echo "Deleting finished"