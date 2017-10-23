/*
 * cumtrapz.h
 *
 * Code generation for function 'cumtrapz'
 *
 */

#ifndef CUMTRAPZ_H
#define CUMTRAPZ_H

/* Include files */
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "tmwtypes.h"
#include "mex.h"
#include "emlrt.h"
#include "covrt.h"
#include "rtwtypes.h"
#include "GetDisplacement_types.h"

/* Function Declarations */
extern void cumtrapz(const emlrtStack *sp, const emxArray_real_T *x,
                     emxArray_real_T *z);

#endif

/* End of code generation (cumtrapz.h) */
