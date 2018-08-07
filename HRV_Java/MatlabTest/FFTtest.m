clc
clear
close all
% test_data = csvread('test_data.csv'); test1_hrv_data.csv
dfs = 1;
rfs = 3;

isresample = 0;
N = dfs*60*2;

test_data = csvread('test1_hrv_data.csv');
% java_data = csvread('test.csv');

if (isresample == 0)
    sampled_data = test_data(1:N);
    fs = dfs;
else 
    xais = (1:1/dfs:N);
    sampled_data = interp1(xais,test_data(1:N),(1:1/rfs:N),'linear');
    fs = rfs;
end


%%


rri = 60./sampled_data;
rr_resamp = rri;
Len_resamp = length(rr_resamp);
% 
N_fft = 2^(nextpow2(Len_resamp));
Y = fft(rr_resamp, N_fft)/Len_resamp;
ff = fs/2 * linspace(0,1,N_fft/2+1);
YY = 2 * abs(Y(1:N_fft/2+1)).^2;
% YY = java_data(:, 1);
% ff = java_data(:, 2);
%% Time domain
max_HR = max(sampled_data);
min_HR = min(sampled_data);

max_min_HR = max_HR - min_HR;
aHR = mean(sampled_data);
%% Frequency domian
ULF = NaN;  
VLF = NaN;

LF = NaN;
LF_peak = NaN;
pLF = NaN;

HF = NaN;
HF_peak = NaN;
pHF = NaN;

ratio_LFHF = NaN;

CR = NaN;

%--------------------
ULF = sum(YY(ff<=0.003));
VLF = sum(YY(ff<=.04 & ff>0.003));

LF = sum(YY(ff<=.15 & ff>0.04));
LF_idx = (YY == max(YY(ff<=.15 & ff>0.04)));
LF_peak = ff(LF_idx);

HF = sum(YY(ff<=.4 & ff>0.15));
HF_idx = (YY == max(YY(ff>0.15 & ff<0.4)));
HF_peak = ff(HF_idx);

pLF = LF/(VLF+HF)*100; % according to what Keith said on Jul. 10th, 2017, spectral peak very sharp but shifts away from 0.1 Hz.
pHF = HF/(VLF+LF)*100;

ratio_LFHF = LF/HF*100; 

cr_cutoff = 0.26; % calcuate the coherence ratio used by emWave
[mLFpeak,~] = max(YY(ff>=0.04 & ff<=cr_cutoff));      
fpeak_id = (YY == mLFpeak);
peakPower = sum(YY(ff>0.015 & ff<ff(fpeak_id)));
mTP = sum(YY(ff<0.4)) - sum(YY(ff<0.0033));
CR = peakPower / (mTP - peakPower)^2;
%%


% 5.4 Return
index_fd = [max_min_HR aHR ULF VLF LF_peak LF HF_peak HF pLF pHF ratio_LFHF CR]

% figure
% plot(ff,YY,'r'); hold on
% plot(java_data(:,2),java_data(:,1),'g');
% axis([0,1,0,max(YY)]);