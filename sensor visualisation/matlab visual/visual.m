str = urlread('https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/Bumps', 'Authentication', 'Basic',...
                'Username','somishopperchousesingetc','Password','6be49dadc1332531c1f128d871d02e05a5469f71') ;
json = parse_json ( str ) ; 
% 
%% disp (json.rows{1}.value) ; 
 jsonRows = json.rows ; 
% disp (json {1} ) 
% disp ( length(json) ) ;
titles = {'rate20' , 'rate50' , 'rate80'} ; 
%disp (titles{1}) ; 

for i = 1: length(jsonRows)
    mat = JsonToMatrix( jsonRows{i}.value) ; 
    disp(mat) ; 
    plotMutliple(mat ,titles ) ; 
    pause ; 
end 
%disp (x ) ;
%plotMutliple ( x) ;



