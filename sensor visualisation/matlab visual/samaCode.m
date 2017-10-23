clc
clear


arr = [ 1 , 2 ,3 , -1 , -2 ] ; 

out = cumtrapz (arr ); 

disp(out) 
% %acceleration = [0 .45 1.79 4.02 7.15 11.18 16.09 21.90 29.05 29.05 29.05 29.05 29.05 22.42 17.9 17.9 17.9 17.9 14.34 11.01 8.9 6.54 2.03 0.55 0];
% acceleration = [0 1 2 3 2 1 0 -1 -2 -3 -2 -1 0 0 0 -1 -2 -3 -2 -1 0 1 2 3 2 1 0 0 0 0 0 ]; 
% time = 0:length(acceleration)-1;
% %time2=[0 2 4 5 6 7 9 12 13 14 17 18 19 20 23 27 28 30 31 32 34 35 36 37 40];
% figure
% plot(time,acceleration,'-*')
% grid on
% title('Acceleration vs Time')
% xlabel('Time (s)')
% ylabel('Acceleration (m^2/s)')
% %distance = trapz(vel)
% cVelocity = cumtrapz(acceleration);
% 
% %cdistance=cumtrapz(time2,acceleration)*40;
% T = table(time',cVelocity','VariableNames',{'Time','CumulativeSpeed'});
% figure
% plot(cVelocity)
% title('Cumulative Velocity Per Second')
% xlabel('Time (s)')
% ylabel('Velocity (m/s)')
% 
% 
% cDistance=cumtrapz(cVelocity);
% disp (length(acceleration) ) ; 
% disp (length(cVelocity) ) ;
% disp (length(cDistance) ) ; 
% 
% T = table(time',cDistance','VariableNames',{'Time','CumulativeDistance'});
% figure
% plot(cDistance)
% title('Cumulative Distance Per Second')
% xlabel('Time (s)')
% ylabel('Distance (m)')