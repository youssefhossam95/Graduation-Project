function out=sampleData(vals,samplingRate)

m=size(vals,2);
out=zeros(1,samplingRate);
divisor=floor(m/samplingRate);
out(1,1)=vals(1,1);
j=2;
for i=2:m
    if ~mod(i,divisor)
        out(1,j)=vals(1,i);
        j=j+1;
    end
end