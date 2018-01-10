display('waiting for the server to respond...') ;
str = urlread('https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/Bumps', 'Authentication', 'Basic',...
                'Username','somishopperchousesingetc','Password','6be49dadc1332531c1f128d871d02e05a5469f71') ;
           
            
display('parsing recived data to json...') ;

json = parse_json ( str ) ; 

jsonRows = json.rows ; 
% display(jsonRows{1}.value)
% display(jsonRows{1}.value.accelVal)
% display(cell2mat(jsonRows{1}.value.accelVal) ) ;
accelValues=zeros(length(jsonRows),1700);
comments=char(zeros(length(jsonRows),20));
fid = fopen('comments.txt','w');
for i = 1: length(jsonRows)
    vals=cell2mat(jsonRows{i}.value.accelVal);
    accelValues(i,1:size(vals,2))=vals;
    comment=jsonRows{i}.value.Comment;
    fprintf(fid,'%s\n',comment);
end
dlmwrite('accelValues.txt',accelValues);
