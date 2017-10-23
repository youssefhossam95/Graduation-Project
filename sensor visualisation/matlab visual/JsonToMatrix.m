function mat = JsonToMatrix (json) 

    
    arr1 = json.Arr1 ; 
    arr2 = json.Arr2 ; 
    arr3 = json.Arr3 ;
    
    maxLength = max( [length(arr1)  length(arr2)  length(arr3) ] )  ; 
    
    mat = zeros ( 3 , maxLength+1 ) ; 
    
    mat(1,1) = length(arr1) ; 
    mat(2,1) = length(arr2) ; 
    mat(3,1) = length(arr3) ; 
    for j = 2 : maxLength+1 
        i = j -1 ; 
        if( i <= length(arr1) ) 
            mat(1 , j ) = arr1(i) ; 
        else 
            mat(1 , j ) = 0 ; 
        end 
        
        if( i <= length(arr2) ) 
            mat(2 , j ) = arr2(i); 
        else 
            mat(2 , j ) = 0 ; 
        end 
        
        if( i <= length(arr3) ) 
            mat(3 , j ) = arr3(i) ; 
        else 
            mat(3 , j ) = 0 ; 
        end 
        
        
    end 
    
    
    


end 