#!/usr/bin/env python

# creates a test file for ROOTObjectsFinder

foutName = "ROOTObjectsFinderTest01.root"

import ROOT
fout = ROOT.TFile(foutName, "RECREATE")

fout.mkdir("subdir1")
fout.mkdir("subdir2")
fout.mkdir("subdir3")
fout.cd("subdir3")
fout.Get("subdir3").mkdir("dirA")

fout.cd("/subdir3/dirA")

histo1 = ROOT.TH1F("histo1","histo1",10,0,1)
histo1.Write()
fout.cd("/subdir1")

histo2 = ROOT.TH1F("histo2","histo2",10,0,1)
histo2.Write()
fout.Close()

