function plotMutliple(toPlotMat , titles )

    [rows cols ] = size(toPlotMat) ; 
    
    maximumValue = max(max(toPlotMat (:, 2:end)))  ; 

    minimumValue = min(min(toPlotMat(: , 2:end)) ) ; 
    for i=1:rows 
        subplot( rows ,1 , i ) ; 
        plot ( toPlotMat(i , 2:toPlotMat(i,1)+1 ))  ; 
        disp(toPlotMat(i ,  2:toPlotMat(i,1)+1 )) ;  
        ylim([ minimumValue maximumValue]); 
        title(titles{i}) ; 
        grid on
        
    end 



end 