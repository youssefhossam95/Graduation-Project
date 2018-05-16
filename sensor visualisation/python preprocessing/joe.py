import FileHandler as FH
rows=FH.loadObjFromFile("MatabsJsonFiles.txt")
count=0
X=[]
y=[]
for row in rows:
    if "Reviewed" not in row["value"] and "Adel" in row["key"]:
        count+=1
print(count)
