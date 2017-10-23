function Displacement = GetDisplacement (acceleration) 
cVelocity = cumtrapz(acceleration);
Displacement=cumtrapz(cVelocity);
end 