display('waiting for the server to respond...') ;
str = urlread('https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/Bumps', 'Authentication', 'Basic',...
                'Username','somishopperchousesingetc','Password','6be49dadc1332531c1f128d871d02e05a5469f71') ;
           
            
display('parsing recived data to json...') ;

json = parse_json ( str ) ; 

jsonRows = json.rows ; 
% display(jsonRows{1}.value)
% display(jsonRows{1}.value.accelVal)
% display(cell2mat(jsonRows{1}.value.accelVal) ) ; 
for i = 1: length(jsonRows)
    plot(cell2mat(jsonRows{i}.value.accelVal) );
    title (jsonRows{i}.value.Comment ) ; 
    display (jsonRows{i}.value.Comment); 
    pause ; 
end 
%disp (x ) ;
%plotMutliple ( x) ;



