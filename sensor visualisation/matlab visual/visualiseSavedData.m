vals=dlmread('accelValues.txt');
fid = fopen('comments.txt','r');
comments=fscanf(fid,'%s');
for i=1:size(vals,1)
    subplot(2,1,1);
    plot(vals(i,:));
    lim=axis();
    ylim=lim(3:4);
    axis([0,size(vals(i,:),2),ylim]);
    title(comments(i,:));
    subplot(2,1,2);
    sampled=sampleData(vals(i,:));
    plot(sampled);
    lim=axis();
    ylim=lim(3:4);
    axis([0,size(sampled,2),ylim]);
    pause;
end