//
// File: GetDisplacement.cpp
//
// MATLAB Coder version            : 3.1
// C/C++ source code generated on  : 21-Oct-2017 11:58:25
//

// Include Files
#include "rt_nonfinite.h"
#include "GetDisplacement.h"
#include "GetDisplacement_emxutil.h"
#include "cumtrapz.h"

// Function Definitions

//
// Arguments    : const emxArray_real_T *acceleration
//                emxArray_real_T *Displacement
// Return Type  : void
//
void GetDisplacement(const emxArray_real_T *acceleration, emxArray_real_T
                     *Displacement)
{
  emxArray_real_T *cVelocity;
  emxInit_real_T(&cVelocity, 2);
  cumtrapz(acceleration, cVelocity);
  cumtrapz(cVelocity, Displacement);
  emxFree_real_T(&cVelocity);
}

//
// File trailer for GetDisplacement.cpp
//
// [EOF]
//
