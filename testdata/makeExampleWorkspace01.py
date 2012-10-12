#!/usr/bin/env python

# creates an example workspace for testing the program /
# creating screenshots

import sys, os

#----------------------------------------------------------------------

#--------------------
outFname = os.path.basename(sys.argv[0])
if outFname.startswith("make"):
    outFname = outFname[4:]

if outFname.endswith(".py"):
    outFname = outFname[:-3]

outFname = outFname[0].lower() + outFname[1:]
outFname += ".root"

#--------------------

# gcs is the 'garbage collection saver'
import ROOT ; gcs = []


# create a workspace object
# (do NOT choose 'ws' as a name because this is already
# used for something else in cint)
ws = ROOT.RooWorkspace("workspace","example workspace")

#--------------------
# a moving Gaussian with varying variance

# mass hypothesis
mhyp = ROOT.RooRealVar("mhyp","mass hypothesis",200,150,250, "GeV/c^2")

# a reconstructed mass variable
mrec = ROOT.RooRealVar("mrec","reconstructed mass",200,150,250, "GeV/c^2")

args = ROOT.RooArgList(); gcs.append(args)
args.add(mhyp)

# the mean of the Gaussian is a function of the mass hypothesis
mean = ROOT.RooFormulaVar("mean","mean of Gaussian", "@0 - 5 * (@0/200)**2", args)

# sigma also depends on mhyp
sigma = ROOT.RooFormulaVar("sigma","sigma of Gaussian", "5 + (@0/200)**2", args)

# create the Gaussian
gaussian = ROOT.RooGaussian("gaussian","Gaussian",mrec, mean, sigma)

# import only the top level object
getattr(ws,'import')(gaussian)

#--------------------


ws.writeToFile(outFname)

