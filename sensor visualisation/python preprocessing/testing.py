a = [ 1 , 1 , 2 , 2 , 1 , 1 , 2 , 2 ] ;
x = sum(a)
n =len(a)
for i in range(0 , len(a) ) :
    a[i]-= x/n
x = sum(a) ;

y = sorted(set(a) ,key=a.count,reverse= True)
print(x)