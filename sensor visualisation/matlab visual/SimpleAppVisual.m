display('waiting for the server to respond...') ;
str = urlread('https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/Bumps', 'Authentication', 'Basic',...
                'Username','somishopperchousesingetc','Password','6be49dadc1332531c1f128d871d02e05a5469f71') ;
           
            
display('parsing recived data to json...') ;

json = parse_json ( str ) ; 

jsonRows = json.rows ; 
% display(jsonRows{1}.value)
% display(jsonRows{1}.value.accelVal)
% display(cell2mat(jsonRows{1}.value.accelVal) ) ; 
gaussWindow=1/4*[1 1 0 1 1];
for i = 1: length(jsonRows)
    comment=jsonRows{i}.value.Comment;
    vals=cell2mat(jsonRows{i}.value.accelVal);
    subplot(2,1,1);
    plot(vals);
    lim=axis();
    ylim=lim(3:4);
    axis([0,size(vals,2),ylim]);
    title(comment);
    subplot(2,1,2);
    diffVals=diff(cell2mat(jsonRows{i}.value.accelVal)); 
    plot(diffVals);
    lim=axis();
    ylim=lim(3:4);
    axis([0,size(diffVals,2),ylim]);
%     gaussVals=conv(vals,gaussWindow);
%     plot(gaussVals);
%     lim=axis();
%     ylim=lim(3:4);
%     axis([0,size(gaussVals,2),ylim]);
    display (jsonRows{i}.value.Comment); 
    pause ; 
end 
%disp (x ) ;
%plotMutliple ( x) ;



