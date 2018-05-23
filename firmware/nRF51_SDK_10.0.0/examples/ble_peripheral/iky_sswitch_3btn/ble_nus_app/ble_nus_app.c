
#include "ble_nus_app.h"
#include "system_config.h"
#include "ble_encrypt.h"
#include "lib\sys_tick.h"
#include "nrf_log.h"
#include "io_control.h"

#ifndef FIRMWARE_VERSION
#define FIRMWARE_VERSION						"1.0.0.0"
#endif

extern bool ioRelay_1_Status;
extern bool ioRelay_2_Status;
extern bool ioRelay_3_Status;
extern bool ioRelay_4_Status;

uint8_t BLE_NUS_PutReport(BLE_NUS_PACKET_TYPE *TxReport, uint8_t opcode,uint8_t * p_data,uint8_t len);

BLE_NUS_PARSER_PACKET_TYPE BLE_NUS__ParserPacket;

BLE_NUS_PACKET_TYPE BLE_NUS__ProtoSend;
BLE_NUS_PACKET_TYPE BLE_NUS__ProtoRecv;
BLE_NUS_PACKET_TYPE BLE_NUS__ProtoReport;

uint8_t BLE_NUS__ProtoSend_Buff[32];
uint8_t BLE_NUS__ProtoRecv_Buff[32];
uint8_t BLE_NUS__ProtoReport_Buff[32];

RINGBUF BLE_NUS_TxRingBuf;
uint8_t BLE_NUS__TxBuf[64];

uint32_t reportFlag = 0;
RESET_PIN_STATE_TYPE rsPINState = RESET_PIN_IDLE;
bool m_nus_xfer_done = false;
ble_nus_t m_nus;

void BLE_NUS_TaskInit(void)
{
	memset(&BLE_NUS__ProtoSend,0,sizeof(BLE_NUS_PACKET_TYPE));
	memset(&BLE_NUS__ProtoRecv,0,sizeof(BLE_NUS_PACKET_TYPE));
	memset(&BLE_NUS__ProtoReport,0,sizeof(BLE_NUS_PACKET_TYPE));
	
	RINGBUF_Init(&BLE_NUS_TxRingBuf,BLE_NUS__TxBuf,sizeof(BLE_NUS__TxBuf));
	
	BLE_NUS__ParserPacket.state = BLE_NUS_CMD_WAITING_SATRT_CODE;
	BLE_NUS__ParserPacket.lenMax = (uint8_t)sizeof(BLE_NUS__ProtoRecv_Buff);
	
	BLE_NUS__ProtoSend.dataPt = BLE_NUS__ProtoSend_Buff;
	BLE_NUS__ProtoRecv.dataPt = BLE_NUS__ProtoRecv_Buff;
	BLE_NUS__ProtoReport.dataPt = BLE_NUS__ProtoReport_Buff;
}

void BLE_NUS_Rx_Callback(uint8_t c)
{
			if(BLE_NUS_ParserPacket(&BLE_NUS__ParserPacket,&BLE_NUS__ProtoRecv,c) == 0)
			{
				BLE_NUS_ProcessData(&BLE_NUS__ProtoRecv,&BLE_NUS__ProtoSend,BLE_NUS__ParserPacket.lenMax);				
			}	
}

void BLE_NUS_Task(void)
{
	uint16_t i;
	uint8_t tmpbuff[16];
	
	BLE_NUS_PutData(&BLE_NUS_TxRingBuf,&BLE_NUS__ProtoSend);
	BLE_NUS_PutData(&BLE_NUS_TxRingBuf,&BLE_NUS__ProtoReport);
	
	if(reportFlag & OUTPUT_REPORT_FLAG)
	{
		reportFlag &= ~OUTPUT_REPORT_FLAG;
		i = 0;
		tmpbuff[i++] = ioRelay_1_Status ? 1 : 0;
		tmpbuff[i++] = ioRelay_2_Status ? 1 : 0;
		tmpbuff[i++] = ioRelay_3_Status ? 1 : 0;
		tmpbuff[i++] = ioRelay_4_Status ? 1 : 0;
		BLE_NUS_PutReport(&BLE_NUS__ProtoReport, PACKET_OPCODE_STATUS, tmpbuff, i);			
	}
	
	if(rsPINState == RESET_PIN_STEP2)
	{
		memset(sysCfg.Password,0,sizeof(sysCfg.Password));
		memcpy((char *)sysCfg.Password,DEFAULT_PASSWORD,sizeof(sysCfg.Password));
		system_config_change = true;
		rsPINState = RESET_PIN_IDLE;
	}
}

void ble_nus_send_task(void)
{
	uint8_t data_array[BLE_NUS_MAX_DATA_LEN];
	uint8_t index = 0;
	static Timeout_Type tNusSendTimeout;
	uint8_t c;
	uint16_t i;
	uint32_t err_code;
	
	if(m_nus_xfer_done && CheckTimeout(&tNusSendTimeout) == SYSTICK_TIMEOUT) //send timeout
	{
		m_nus_xfer_done = false;
	}
	
	if(m_nus_xfer_done) return; //sending
		
	for(i=0;i<BLE_NUS_MAX_DATA_LEN;i++)
	{
		if(RINGBUF_Get(&BLE_NUS_TxRingBuf,&c) == 0)
		{
			data_array[index] = c;
			index++;
		}
		else
		{
			 break; //data empty
		}
	}
	
	if (index)
	{
		NRF_LOG_PRINTF("ble nus send %d bytes ...",index);
		err_code = ble_nus_string_send(&m_nus, data_array,index);
		if(err_code == NRF_SUCCESS)
		{
			m_nus_xfer_done = true;
			NRF_LOG_PRINTF("success\r\n");
		}
		else
		{
			m_nus_xfer_done = false;
			NRF_LOG_PRINTF("fail %x\r\n",err_code);
		}
		
		if(m_nus_xfer_done) InitTimeout(&tNusSendTimeout,500);		
		index = 0;
	}
}

uint8_t BLE_NUS_ParserPacket(BLE_NUS_PARSER_PACKET_TYPE *parserPacket,BLE_NUS_PACKET_TYPE *IKYBLEProtoRecv,uint8_t c)
{
	switch(parserPacket->state)
	{
		case BLE_NUS_CMD_WAITING_SATRT_CODE:
			if(c == 0xCA)
			{
				parserPacket->state = BLE_NUS_CMD_GET_OPCODE;
				parserPacket->len = 0;
				parserPacket->crc = 0;
				parserPacket->cnt = 0;
			}			
			break;
			
		case BLE_NUS_CMD_GET_OPCODE:
			parserPacket->opcode = c;
			parserPacket->state = BLE_NUS_CMD_GET_LENGTH;
			break;
		
		case BLE_NUS_CMD_GET_LENGTH:		
			parserPacket->len = c;
			parserPacket->state = BLE_NUS_CMD_GET_DATA;
			break;
		
		case BLE_NUS_CMD_GET_DATA:
			if((parserPacket->cnt >= parserPacket->len) || (parserPacket->len > parserPacket->lenMax))
			{
				parserPacket->state = BLE_NUS_CMD_WAITING_SATRT_CODE;
			}
			else
			{
				parserPacket->crc += c;
				IKYBLEProtoRecv->dataPt[parserPacket->cnt]= c;
				parserPacket->cnt++;
				if(parserPacket->cnt == parserPacket->len)
				{
						parserPacket->state = BLE_NUS_CMD_CRC_CHECK;
				}
			}
			break;
			
		case BLE_NUS_CMD_CRC_CHECK:
			parserPacket->state= BLE_NUS_CMD_WAITING_SATRT_CODE;
			if(parserPacket->crc  == c)
			{	
					IKYBLEProtoRecv->length = parserPacket->len;
					IKYBLEProtoRecv->opcode = parserPacket->opcode;
					return 0;
			}
			break;
			
		default:
			parserPacket->state = BLE_NUS_CMD_WAITING_SATRT_CODE;
			break;
	}
	return 0xff;
}

uint8_t BLE_NUS_ProcessData(BLE_NUS_PACKET_TYPE *IKYBLEProtoRecv,BLE_NUS_PACKET_TYPE *IKYBLEProtoSend,uint32_t maxPacketSize)
{
	uint8_t ResponseCode = RESPONSE_CODE_APPROVED;
	uint8_t ResponseFlag = 1;
	uint32_t u32Temp = 0;
	uint32_t u32PINRecv = 0xF6B41C65;
	uint8_t PINisOK = 0;
	uint8_t u8Tmp1[16];
	uint8_t u8Tmp2[16];

	//verify PIN
	if(IKYBLEProtoRecv->length >= DEVICE_PASSWORD_LENGTH)
	{
		memcpy(u8Tmp1,sysCfg.Password,DEVICE_PASSWORD_LENGTH);
		memcpy(&u8Tmp1[DEVICE_PASSWORD_LENGTH],&IKYBLEProtoRecv->dataPt[ENCRYPT_BLOCK_PIN_LENGTH],IKYBLEProtoRecv->length - ENCRYPT_BLOCK_PIN_LENGTH);
		u32Temp = BLE_CalcPINBlock(u8Tmp1,DEVICE_PASSWORD_LENGTH + IKYBLEProtoRecv->length - ENCRYPT_BLOCK_PIN_LENGTH);
		u32PINRecv = 	(uint32_t)IKYBLEProtoRecv->dataPt[0]<<24;
		u32PINRecv |= (uint32_t)IKYBLEProtoRecv->dataPt[1]<<16;
		u32PINRecv |= (uint32_t)IKYBLEProtoRecv->dataPt[2]<<8;
		u32PINRecv |= (uint32_t)IKYBLEProtoRecv->dataPt[3];
		if(u32Temp == u32PINRecv) 
		{
			PINisOK = 1;
			NRF_LOG_PRINTF("\r\nPIN is OK\r\n");
			memcpy(u8Tmp2,sysCfg.Password,DEVICE_PASSWORD_LENGTH);
		}
	}
	else 
	{
		return 0;
	}

	NRF_LOG_PRINTF("\r\nOPCODE %x\r\n",IKYBLEProtoRecv->opcode);
	//
	switch(IKYBLEProtoRecv->opcode)
	{
		case PACKET_OPCODE_LOGIN:
			if(!PINisOK)
			{
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
				break;
			}
			break;
			
		case PACKET_OPCODE_STATUS:
			if(!PINisOK){
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
			}
			else
			{
				ResponseFlag = 0;
				reportFlag |= OUTPUT_REPORT_FLAG;
			}
			break;
		
		case PACKET_OPCODE_NAME:
			if(!PINisOK){
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
			}
			else {			
				if(IKYBLEProtoRecv->length < sizeof(sysCfg.DeviceName)) {
					if(io_buzzer.enable != IO_TOGGLE_ENABLE)
					{
						IO_ToggleSetStatus(&io_buzzer,100,100,IO_TOGGLE_ENABLE,1);
					}
					memset(sysCfg.DeviceName,0x00,sizeof(sysCfg.DeviceName));
					memcpy(sysCfg.DeviceName,&IKYBLEProtoRecv->dataPt[4],IKYBLEProtoRecv->length - 4);					
					system_config_change = true;
					ResetMcuSet(MCU_RESET_AFTER_1_SEC);
				}
				else{
					ResponseCode = RESPONSE_CODE_SYSTEM_MALFUNCTION;
				}
			}
			break;
			
		case PACKET_OPCODE_PIN:
			if(!PINisOK){
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
				break;
			}
			if(IKYBLEProtoRecv->length != 12){
				ResponseCode = RESPONSE_CODE_SYSTEM_MALFUNCTION;
			}
			else {
				if(io_buzzer.enable != IO_TOGGLE_ENABLE)
				{
					IO_ToggleSetStatus(&io_buzzer,100,100,IO_TOGGLE_ENABLE,1);
				}
				memset(u8Tmp1,0,sizeof(u8Tmp1));
				BLE_Decrypt(u8Tmp1,u8Tmp2,&IKYBLEProtoRecv->dataPt[ENCRYPT_BLOCK_PIN_LENGTH]);
				memset(sysCfg.Password,0,sizeof(sysCfg.Password));
				memcpy(sysCfg.Password,u8Tmp1,DEVICE_PASSWORD_LENGTH);
				system_config_change = true;
			}
			break;
			
		case PACKET_OPCODE_CMD1:
			if(!PINisOK){
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
				break;
			}
			IO_ToggleSetStatus(&io_buzzer,50,100,IO_TOGGLE_ENABLE,1);
			ioRelay_1_Status = IKYBLEProtoRecv->dataPt[4];
			ResponseFlag = 0;
			reportFlag |= OUTPUT_REPORT_FLAG;
			break;
		
		case PACKET_OPCODE_CMD2:
			if(!PINisOK){
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
				break;
			}
			IO_ToggleSetStatus(&io_buzzer,50,100,IO_TOGGLE_ENABLE,2);
			ioRelay_2_Status = IKYBLEProtoRecv->dataPt[4];
			ResponseFlag = 0;
			reportFlag |= OUTPUT_REPORT_FLAG;
			break;
		
		case PACKET_OPCODE_CMD3:
			if(!PINisOK){
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
				break;
			}
			IO_ToggleSetStatus(&io_buzzer,50,100,IO_TOGGLE_ENABLE,3);
			ioRelay_3_Status = IKYBLEProtoRecv->dataPt[4];
			ResponseFlag = 0;
			reportFlag |= OUTPUT_REPORT_FLAG;
			break;
		
		case PACKET_OPCODE_CMD4:
			if(!PINisOK){
				ResponseCode = RESPONSE_CODE_INCORRECT_PIN;
				break;
			}
			IO_ToggleSetStatus(&io_buzzer,50,100,IO_TOGGLE_ENABLE,4);
			ioRelay_4_Status = IKYBLEProtoRecv->dataPt[4];
			ResponseFlag = 0;
			reportFlag |= OUTPUT_REPORT_FLAG;
			break;
		
			
		case PACKET_OPCODE_FWVER:						
			ResponseFlag = 0;
			//response
			IKYBLEProtoSend->start = 0xCA;
			IKYBLEProtoSend->opcode = PACKET_OPCODE_FWVER;
			IKYBLEProtoSend->length = strlen(FIRMWARE_VERSION);
			memcpy(IKYBLEProtoSend->dataPt,FIRMWARE_VERSION,IKYBLEProtoSend->length);			
			IKYBLEProtoSend->crc = BLE_CalcCheckSum(IKYBLEProtoSend->dataPt,IKYBLEProtoSend->length);	
			break;	
		
		default:
			IO_ToggleSetStatus(&io_buzzer,50,100,IO_TOGGLE_ENABLE,1);
			ResponseFlag = 0;
			break;
	}
	if(ResponseFlag)
	{
		IKYBLEProtoSend->start = 0xCA;
		IKYBLEProtoSend->opcode = IKYBLEProtoRecv->opcode;
		IKYBLEProtoSend->length = 1;
		IKYBLEProtoSend->dataPt[0] = ResponseCode;
		IKYBLEProtoSend->crc = BLE_CalcCheckSum(IKYBLEProtoSend->dataPt,IKYBLEProtoSend->length);
	}
	IKYBLEProtoRecv->opcode = 0;
	return 0;
}


uint8_t BLE_NUS_PutReport(BLE_NUS_PACKET_TYPE *TxReport,uint8_t opcode,uint8_t * p_data,uint8_t len)
{
	uint32_t u32Temp;	
	uint8_t* u8pt1 = TxReport->dataPt;
	uint8_t* u8pt2;
	//
	memcpy(u8pt1,sysCfg.Password,DEVICE_PASSWORD_LENGTH);
	memcpy(&u8pt1[DEVICE_PASSWORD_LENGTH],p_data,len);
	u32Temp = BLE_CalcPINBlock(u8pt1,DEVICE_PASSWORD_LENGTH + len);
	u8pt2 =  (uint8_t*)&u32Temp;
	u8pt1[0] = u8pt2[0];
	u8pt1[1] = u8pt2[1];
	u8pt1[2] = u8pt2[2];
	u8pt1[3] = u8pt2[3];			
	//
	TxReport->start = 0xCA;
	TxReport->opcode = opcode;
	TxReport->length = ENCRYPT_BLOCK_PIN_LENGTH + len;	
	TxReport->crc = BLE_CalcCheckSum(TxReport->dataPt,TxReport->length);
	return 0;
}


uint8_t BLE_NUS_PutData(RINGBUF *TxRingbuf,BLE_NUS_PACKET_TYPE * TxData)
{
	uint8_t i = 0;
	uint8_t length = 0;
	if(TxData->length)
	{
		RINGBUF_Put(TxRingbuf,TxData->start);
		RINGBUF_Put(TxRingbuf,TxData->opcode);	
		RINGBUF_Put(TxRingbuf,TxData->length);			
		for(i = 0;i < TxData->length;i++)
		{
			RINGBUF_Put(TxRingbuf,TxData->dataPt[i]);			
		}
		RINGBUF_Put(TxRingbuf,TxData->crc);		
		TxData->length = 0;	
		return length;	
	}
	return 0;
}




