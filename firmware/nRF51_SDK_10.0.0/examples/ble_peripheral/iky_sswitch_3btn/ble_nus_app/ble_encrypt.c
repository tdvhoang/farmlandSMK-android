
#include "ble_encrypt.h"
#include "md5.h"
#include "d3des.h"
/****************************************************************************
  * @brief  Calculator md5 from src + key
  * @param  @dstHex: string result in hex
						@src: string calculator MD5
						@src len: len of src
  * @retval None
****************************************************************************/
void HashMD5(uint8_t *dst,uint8_t *src,uint16_t srcLen)
{
	md5_state_t state;
	md5_init(&state);
	md5_append(&state, (const md5_byte_t *)src, srcLen);
	md5_finish(&state, dst);
}

void BLE_Decrypt(uint8_t* Dst, uint8_t* MaterKey, uint8_t* planMsg)
{	
	uint8_t PW[24 + 1] = { 0 };
	memset(PW,0,sizeof(PW));
	memcpy(&PW[0],MaterKey,4);
	memcpy(&PW[4],MaterKey,4);
	memcpy(&PW[8],MaterKey,4);
	memcpy(&PW[12],MaterKey,4);
	memcpy(&PW[16],MaterKey,4);
	memcpy(&PW[20],MaterKey,4);
	//3des
	TDES_keyschedule_dec(PW);
	TDES_decrypt(planMsg, Dst);	
}

uint8_t BLE_CalcCheckSum(uint8_t *buff, uint16_t length)
{
	uint32_t i;
	uint8_t crc = 0;
	for(i = 0;i < length; i++)
	{
		crc += buff[i];
	}
	return crc;
}

uint32_t BLE_CalcPINBlock(uint8_t *buff, uint16_t length)
{
	uint32_t u32crc = 0;
	uint8_t *u8pt;
	uint8_t u8crc[16+1] = {0};
	HashMD5(u8crc,buff,length);
	u8pt = (uint8_t*)&u32crc;
	u8pt[0] = u8crc[0] ^ u8crc[1] ^ u8crc[2] ^ u8crc[3];
	u8pt[1] = u8crc[4] ^ u8crc[5] ^ u8crc[6] ^ u8crc[7];
	u8pt[2] = u8crc[8] ^ u8crc[9] ^ u8crc[10] ^ u8crc[11];
	u8pt[3] = u8crc[12] ^ u8crc[13] ^ u8crc[14] ^ u8crc[15]; 
	return u32crc;
}
/*******************************************************************************
	*END FILE
*******************************************************************************/




