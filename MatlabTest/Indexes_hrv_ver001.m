%==========================================================================
%Indexes_hrv.m    created by Ping Cheng, ping.cheng@wisdomaic.com
%   - to extract performance indexes using RR interval series
%   
%   - Input
%       - RR interval series, in ms
%   - Ouput
%       - Time domain
%           -max_min_HR: average difference between the highest HR and the lowest HR in each respiratory circle during at least 2-min time duration
%           -aHR: average heart rate
%       - Frequency domain
%           -ULF: absolute power of the ultra-low-frequency band (<=0.003 Hz)
%           -VLF: absolute power of the very-low-frequency band (0.003~0.04 Hz)
%           -LF:  absolute power of the low-frequency band (0.04~0.15 Hz)
%           -LF_peak: peak frequency of the low-frequency band (0.04~0.15 Hz)
%           -HF: absolute power of the high-frequency band
%           -HF_peak: peak frequency of the high-frequency band (0.15~0.4 Hz)
%           -pLF: relative power of LF power, suggested to calculate by (LF power) / (VFL power + HF power)
%           -pHF: relative power of HF power, suggested to calculate by (HF power) / (VFL power + LF power)
%           -ratio_LFHF: ratio of LF power to HF power
%           -CR: the power in (0.015 ~ peak frequency Hz) to the power in (0.003 ~ 0.4 Hz) 
%   - ver001 created on Jul. 12, 2018
%==========================================================================

clear all
close all

flag_debug = 1;
rec = 3;
idx_matrix = [];
figure(1);


%% Block 0: Global parameter definition
overlap = 0.80; % the overlap probability between two successive data windows
windowLen = 120; % in seconds
Fs = 50; % in Hz, sampling rate

%% Block 1: Get pulse data batch

filename = ['emWave\' 'emWave_' num2str(rec) 'T_RR.mat'];
load(filename);
rri = rri/1000;


if overlap == 1
    display('No sliding window is used.')
else  
    steps = ceil(windowLen*(1-overlap));  % newly add data amount
end


for j = windowLen:steps:length(rri)
    
    
%% Block 2: Filtering & Motion artifact removal


%% Block 3: Peak detection
%rri2 = get_rri2(); % a series of RR interval series
    rri2 = rri(max([1 j-windowLen+1]):j);

%% Block 4: Performance indexes in the time domain
    % 4.1 parameter initialization
    max_min_HR = NaN(1);
    aHR = NaN(1);

    % 4.2 index extraction
    max_min_HR = max_min_hrv(rri2);
    aHR = mean(60 ./ rri2);


%% Block 5: Performance indexes in the frequency domain
    % 5.1 parameter initialization
    fs = 3; % resampling rate  %**********
    interpl_type = 'spline';
    index_fd = NaN(12);

    
    % 5.2 interpolation
    switch interpl_type
        case 'none'
            rr_resamp = rri2;
        otherwise
            if length(rri2)>1 && (sum(isnan(rri2))==0)
                x_axis = cumsum(rri2)-rri2(1);
                rr_resamp = interp1(x_axis, rri2, 0:1/fs:x_axis(end), interpl_type);
            else
                rr_resamp = [];
            end
    end


    % 5.2 FFT
    Len_resamp = length(rr_resamp);

    if Len_resamp == 0
        error('RR interval series is empty. Please check !');
    else
        N_fft = 2^(nextpow2(Len_resamp));
        Y = fft(rr_resamp, N_fft)/Len_resamp;
        ff = fs/2 * linspace(0,1,N_fft/2+1);   %**********
        YY = 2 * abs(Y(1:N_fft/2+1)).^2;   %********the square
        
        if flag_debug==1
%             figure()
%             plot(ff,YY,'.-')
%             title('PSD of HR')
%             pause(0.1)
            
%             figure()
            plot(ff(ff<0.4),YY(ff<0.4),'g-')
            title('PSD of HR in (0~0.4 Hz)')
            pause(0.5)
        end
    end

    % 5.3 Index extraction
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
    CR = peakPower / (mTP - peakPower);

    % 5.4 Return
    index_fd = [max_min_HR aHR ULF VLF LF_peak LF  HF_peak HF pLF pHF ratio_LFHF CR];
 
    idx_matrix = [idx_matrix; index_fd];

end

