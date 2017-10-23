/*
 * cumtrapz.c
 *
 * Code generation for function 'cumtrapz'
 *
 */

/* Include files */
#include "rt_nonfinite.h"
#include "GetDisplacement.h"
#include "cumtrapz.h"
#include "GetDisplacement_emxutil.h"

/* Variable Definitions */
static emlrtRTEInfo b_emlrtRTEI = { 20, 9, "cumtrapz",
  "C:\\Program Files\\MATLAB\\R2016a\\toolbox\\eml\\lib\\matlab\\datafun\\cumtrapz.m"
};

/* Function Definitions */
void cumtrapz(const emlrtStack *sp, const emxArray_real_T *x, emxArray_real_T *z)
{
  int32_T iyz;
  real_T s;
  real_T ylast;
  int32_T k;
  iyz = z->size[0] * z->size[1];
  z->size[0] = 1;
  z->size[1] = x->size[1];
  emxEnsureCapacity(sp, (emxArray__common *)z, iyz, (int32_T)sizeof(real_T),
                    &b_emlrtRTEI);
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

/* End of code generation (cumtrapz.c) */
