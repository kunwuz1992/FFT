			if(PR_For_FFT_Index<30)
            {
            	if (nPR != 0){
					//Resample the nPR with the new sampling rate RESAMPLE_HZ
					if(PR_For_FFT_Index == 0) _PR_For_Index.add((double) nPR);
					endPR = _PR_For_Index.get(_PR_For_Index.size() - 1);
					diff = ((double) nPR - endPR)/RESAMPLE_HZ;
					for (int i = 1; i <= RESAMPLE_HZ; i++)
					{
						_PR_For_Index.add(endPR+diff*i);
					}
                	PR_For_FFT_Index ++;
            	}
            }else
            {
				_Index_From_PR.clear();
				endPR = _PR_For_Index.get(_PR_For_Index.size() - 1);
				diff = ((double) nPR - endPR)/RESAMPLE_HZ;
				for (int i = 1; i <= RESAMPLE_HZ; i++)
				{
					_PR_For_Index.remove(0);
					_PR_For_Index.add(endPR+diff*i);
				}
                FFTCal fftCal = new FFTCal(_PR_For_Index,_Index_From_PR,RESAMPLE_HZ);
                fftCal.indexcal();
				if(_PDIType.equals("pLF"))
					PIDATA.add(_Index_From_PR.get(0).intValue());
				else if(_PDIType.equals("pLF1"))
					PIDATA.add(_Index_From_PR.get(1).intValue());
				else if(_PDIType.equals("pLF2"))
					PIDATA.add(_Index_From_PR.get(2).intValue());
				else if(_PDIType.equals("CR"))
					PIDATA.add(_Index_From_PR.get(3).intValue());
            }