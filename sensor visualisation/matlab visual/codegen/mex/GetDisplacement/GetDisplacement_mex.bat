@echo off
set MATLAB=C:\PROGRA~1\MATLAB\R2016a
set MATLAB_ARCH=win64
set MATLAB_BIN="C:\Program Files\MATLAB\R2016a\bin"
set ENTRYPOINT=mexFunction
set OUTDIR=.\
set LIB_NAME=GetDisplacement_mex
set MEX_NAME=GetDisplacement_mex
set MEX_EXT=.mexw64
call setEnv.bat
echo # Make settings for GetDisplacement > GetDisplacement_mex.mki
echo COMPILER=%COMPILER%>> GetDisplacement_mex.mki
echo COMPFLAGS=%COMPFLAGS%>> GetDisplacement_mex.mki
echo OPTIMFLAGS=%OPTIMFLAGS%>> GetDisplacement_mex.mki
echo DEBUGFLAGS=%DEBUGFLAGS%>> GetDisplacement_mex.mki
echo LINKER=%LINKER%>> GetDisplacement_mex.mki
echo LINKFLAGS=%LINKFLAGS%>> GetDisplacement_mex.mki
echo LINKOPTIMFLAGS=%LINKOPTIMFLAGS%>> GetDisplacement_mex.mki
echo LINKDEBUGFLAGS=%LINKDEBUGFLAGS%>> GetDisplacement_mex.mki
echo MATLAB_ARCH=%MATLAB_ARCH%>> GetDisplacement_mex.mki
echo BORLAND=%BORLAND%>> GetDisplacement_mex.mki
echo OMPFLAGS= >> GetDisplacement_mex.mki
echo OMPLINKFLAGS= >> GetDisplacement_mex.mki
echo EMC_COMPILER=msvc140>> GetDisplacement_mex.mki
echo EMC_CONFIG=optim>> GetDisplacement_mex.mki
"C:\Program Files\MATLAB\R2016a\bin\win64\gmake" -B -f GetDisplacement_mex.mk
