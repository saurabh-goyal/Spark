import numpy as np
import math
#import matplotlib.pyplot as plt
from pylab import *

def calculateFlux(wavelength, temprature=5000):
	c1 = 3.74 * pow(10, -16)
	c2 = 1.44 * pow(10, -2)

	nr = c1 * pow(wavelength, -5)
	t = np.float64(c2/(wavelength * temprature))
	t = np.exp(t)
	dr = t -1 
	flux = nr/dr
	return flux

def fluxAtEarth(flux, radius, distance):
	return flux * pow(radius/distance, 2)

def wavelengthForPeakEmission(temprature):
	return 0.0029/temprature

def peakEmission(temprature):
	return calculateFlux(wavelengthForPeakEmission(temprature), temprature)

def ceilToNext10Power(value):
	l = math.log10(value)
	l = math.ceil(l)
	return pow(10, l)

def showGraph(temprature):
	ax = subplot(111)
	t = np.arange(0.0, 2000.0, 1.00)
	s = list(map(lambda x:calculateFlux(x*pow(10, -9), temprature), t))
	line, = plot(t, s, lw=1)
	ylim(0, peakEmission(temprature)*1.2)
	show()

def fluxIntegrationAt(wavelength, temprature):
    c1 = 3.74 * pow(10, -16)
    c2 = 1.44 * pow(10, -2)
    x = 1/wavelength
    c = -1 * c2/temprature
    a = exp(c*x)
    
    value = 0.0
 
    value = x - 1/c
    value *= 2/c
    value = pow(x,2) - value
    value *= 3/c
    value -= pow(x, 3)
    value *= a/c
    return c1*value

def areaUnderCurveForFlux(wavelengthStart, width, temprature):
	b = fluxIntegrationAt(wavelengthStart+width, temprature)
	a = fluxIntegrationAt(wavelengthStart, temprature)
	value = b-a
	return value

def integrateFlux(start, width, temprature, n):
	h = width/n
	s = 0.0
	m = 0
	x = start
	for i in (0, n):
		y = calculateFlux(x, temprature)
		if (x == start) | (x == (start+width)) :
			s += y
		else:
			if m==0:
				m=1
			else:
				m=0
			s += y * (m+1) * 2.0
		x += h
	return s * h/3.0

def magnitude(refMagnitude, refFlux, flux):
    return refMagnitude - 2.5*math.log10(flux/refFlux)

def uFlux(temprature):
        return areaUnderCurve((365-66/2) * pow(10, -9), 66 * pow(10, -9), temprature)
def vFlux(temprature):
        return areaUnderCurve((445-94/2) * pow(10, -9), 94 * pow(10, -9), temprature)
def bFlux(temprature):
        return areaUnderCurve((551-88/2) * pow(10, -9), 88 * pow(10, -9), temprature)
def rFlux(temprature):
        return areaUnderCurve((658-138/2) * pow(10, -9), 138 * pow(10, -9), temprature)


#temprature = 5000
#a = integrateFlux((365-66/2) * pow(10, -9), 66 * pow(10, -9), temprature, 1000)
#b = areaUnderCurveForFlux((365-66/2) * pow(10, -9), 66 * pow(10, -9), temprature)
#print(a, b)

showGraph(4000)

