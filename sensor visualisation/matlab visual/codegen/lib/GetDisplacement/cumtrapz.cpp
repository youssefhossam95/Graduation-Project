//
// File: cumtrapz.cpp
//
// MATLAB Coder version            : 3.1
// C/C++ source code generated on  : 21-Oct-2017 11:58:25
//

// Include Files
#include "rt_nonfinite.h"
#include "GetDisplacement.h"
#include "cumtrapz.h"
#include "GetDisplacement_emxutil.h"

// Function Definitions

//
// Arguments    : const emxArray_real_T *x
//                emxArray_real_T *z
// Return Type  : void
//
void cumtrapz(const emxArray_real_T *x, emxArray_real_T *z)
{
  int iyz;
  double s;
  double ylast;
  int k;
  iyz = z->size[0] * z->size[1];
  z->size[0] = 1;
  z->size[1] = x->size[1];
  emxEnsureCapacity((emxArray__common *)z, iyz, (int)sizeof(double));
  if (!(x->size[1] == 0)) {
    s = 0.0;
    iyz = 0;
    ylast = x->data[0];
    z->data[0] = 0.0;
    for (k = 0; k <= x->size[1] - 2; k++) {
      iyz++;
      s += (ylast + x->data[iyz]) / 2.0;
      ylast = x->data[iyz];
      z->data[iyz] = s;
    }
  }
}

//
// File trailer for cumtrapz.cpp
//
// [EOF]
//
