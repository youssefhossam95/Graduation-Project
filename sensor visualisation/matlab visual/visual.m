str = urlread('https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/bumpsdb/_design/GetAllData/_view/ReturnAllBumps', 'Authentication', 'Basic',...
                'Username','thereirdietsithadeentlyi','Password','9d1366a8d0151ceba314b4ace10d62d51694d075') ;
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



