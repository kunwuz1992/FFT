# WAV 文件格式介紹

## 1.概述

`Waveform Audio File Format`（`WAVE`，又或者是因为`WAV`后缀而被大众所知的），它采用`RIFF`（Resource Interchange File Format）文件格式结构。通常用来保存`PCM`格式的原始音频数据，所以通常被称为无损音频。但是严格意义上来讲，`WAV`也可以存储其它压缩格式的音频数据。

![img](http://s4.51cto.com/wyfs02/M00/7D/AD/wKioL1btPw_xefJSAABiQscQxNA841.png) 

## 2.格式解析

`WAV`文件遵循RIFF规则，其内容以区块（`chunk`）为最小单位进行存储。`WAV`文件一般由3个区块组成：`RIFF chunk`、`Format chunk`和`Data chunk`。另外，文件中还可能包含一些可选的区块，如：`Fact chunk`、`Cue points chunk`、`Playlist chunk`、`Associated data list chunk`等。
 本文将只介绍`RIFF chunk`、`Format chunk`和`Data chunk`。

#### 2.1 RIFF区块

| 名称 | 偏移地址 | 字节数 | 端序 | 内容                |
| ---- | -------- | ------ | ---- | ------------------- |
| ID   | 0x00     | 4Byte  | 大端 | 'RIFF' (0x52494646) |
| Size | 0x04     | 4Byte  | 小端 | fileSize - 8        |
| Type | 0x08     | 4Byte  | 大端 | 'WAVE'(0x57415645)  |

- 以`'RIFF'`为标识
-  `Size`是整个文件的长度减去`ID`和`Size`的长度
-  `Type`是`WAVE`表示后面需要两个子块：`Format`区块和`Data`区块

#### 2.2 FORMAT区块

| 名称          | 偏移地址 | 字节数 | 端序 | 内容                |
| ------------- | -------- | ------ | ---- | ------------------- |
| ID            | 0x00     | 4Byte  | 大端 | 'fmt ' (0x666D7420) |
| Size          | 0x04     | 4Byte  | 小端 | 16                  |
| AudioFormat   | 0x08     | 2Byte  | 小端 | 音频格式            |
| NumChannels   | 0x0A     | 2Byte  | 小端 | 声道数              |
| SampleRate    | 0x0C     | 4Byte  | 小端 | 采样率              |
| ByteRate      | 0x10     | 4Byte  | 小端 | 每秒数据字节数      |
| BlockAlign    | 0x14     | 2Byte  | 小端 | 数据块对齐          |
| BitsPerSample | 0x16     | 2Byte  | 小端 | 采样位数            |

- 以`'fmt' `为标识
-  `Size`表示该区块数据的长度（不包含`ID`和`Size`的长度）
-  `AudioFormat`表示`Data`区块存储的音频数据的格式，`PCM`音频数据的值为1
-  `NumChannels`表示音频数据的声道数，1：单声道，2：双声道
-  `SampleRate`表示音频数据的采样率
-  `ByteRate`每秒数据字节数 = SampleRate * NumChannels * BitsPerSample / 8
-  `BlockAlign`每个采样所需的字节数 = NumChannels * BitsPerSample / 8
-  `BitsPerSample`每个采样存储的bit数，8：8bit，16：16bit，32：32bit

#### 2.3 DATA区块

| 名称 | 偏移地址 | 字节数 | 端序 | 内容                |
| ---- | -------- | ------ | ---- | ------------------- |
| ID   | 0x00     | 4Byte  | 大端 | 'data' (0x64617461) |
| Size | 0x04     | 4Byte  | 小端 | N                   |
| Data | 0x08     | NByte  | 小端 | 音频数据            |

- 以`'data'`为标识
-  `Size`表示音频数据的长度，N = ByteRate * seconds
-  `Data`音频数据

## 3. 小端存储

`WAV`文件以小端形式来进行数据存储。

> 所谓的大端模式，是指数据的低位保存在内存的高地址中，而数据的高位，保存在内存的低地址中；
>  所谓的小端模式，是指数据的低位保存在内存的低地址中，而数据的高位保存在内存的高地址中。

下面解释一下PCM数据在`WAV`文件中的bit位排列方式

| PCM数据类型  | 采样                 | 采样                 |
| ------------ | -------------------- | -------------------- |
| 8Bit 单声道  | 声道0                | 声道0                |
| 8Bit 双声道  | 声道0                | 声道1                |
| 16Bit 单声道 | 声道0低位，声道0高位 | 声道0低位，声道0高位 |
| 16Bit 双声道 | 声道0低位，声道0高位 | 声道1低位，声道1高位 |

## 4. 音频加速减速设计
​	1) 读取原始音频文件并转换成Byte
	2) 分离WAV文件的`HEAD`和`DATA`
	3) 根据需求对`DATA`进行resample，并修改`HEAD`中`RIFF.SIZE`和`DATA.SIZE`.









