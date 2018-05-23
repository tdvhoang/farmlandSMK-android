#ifndef __IKY_BLE_APP_H__
#define __IKY_BLE_APP_H__

#include <stdint.h>
#include <stdbool.h>
#include "lib\ringbuf.h"
#include "lib\sys_tick.h"
#include "ble_nus.h"

#define PACKET_OPCODE_LOGIN								0x01
#define PACKET_OPCODE_CMD1								0x02
#define PACKET_OPCODE_CMD2								0x03
#define PACKET_OPCODE_CMD3								0x04
#define PACKET_OPCODE_CMD4								0x05
#define PACKET_OPCODE_FWVER								0x06
#define PACKET_OPCODE_PIN									0x07
#define PACKET_OPCODE_NAME								0x08
#define PACKET_OPCODE_STATUS							0x09

#define ENCRYPT_BLOCK_PIN_LENGTH					4

#define RESPONSE_CODE_APPROVED						0x00
#define RESPONSE_CODE_INCORRECT_PIN				0x55
#define RESPONSE_CODESECURITY_VIOLATION		0x63
#define RESPONSE_CODE_SYSTEM_MALFUNCTION	0x96


#define OUTPUT_REPORT_FLAG								0x01
#define OUTPUT_TOUCH_CONTROL_FLAG					0x02

typedef struct __attribute__((packed)){
	uint8_t start;
	uint8_t opcode;
	uint8_t length;
	uint8_t *dataPt;
	uint8_t crc;
}BLE_NUS_PACKET_TYPE;

typedef enum{
	BLE_NUS_CMD_NEW_STATE,
	BLE_NUS_CMD_GET_LENGTH,
	BLE_NUS_CMD_GET_OPCODE,
	BLE_NUS_CMD_GET_DATA,
	BLE_NUS_CMD_CRC_CHECK,
	BLE_NUS_CMD_WAITING_SATRT_CODE
}BLE_NUS_CMD_STATE_TYPE;

typedef struct
{
	BLE_NUS_CMD_STATE_TYPE state;
	uint8_t len;
	uint8_t lenMax;
	uint8_t cnt;
	uint8_t opcode;
	uint8_t crc;
}BLE_NUS_PARSER_PACKET_TYPE;

typedef enum{
	RESET_PIN_IDLE,
	RESET_PIN_STEP1,
	RESET_PIN_STEP2,
}RESET_PIN_STATE_TYPE;

extern RINGBUF IKYBLE_TxRingBuf;
extern RINGBUF IKYBLE_RxRingBuf;
extern BLE_NUS_PACKET_TYPE IKYBLE__ProtoReport;
extern uint32_t reportFlag;
extern RESET_PIN_STATE_TYPE rsPINState;
extern bool m_nus_xfer_done;
extern ble_nus_t m_nus;

void BLE_NUS_Rx_Callback(uint8_t c);
void BLE_NUS_TaskInit(void);
void BLE_NUS_Task(void);
uint8_t BLE_NUS_ParserPacket(BLE_NUS_PARSER_PACKET_TYPE *parserPacket,BLE_NUS_PACKET_TYPE *IKYBLEProtoRecv,uint8_t c);
uint8_t BLE_NUS_ProcessData(BLE_NUS_PACKET_TYPE *IKYBLEProtoRecv,BLE_NUS_PACKET_TYPE *IKYBLEProtoSend,uint32_t maxPacketSize);
uint8_t BLE_NUS_PutData(RINGBUF *TxRingbuf,BLE_NUS_PACKET_TYPE * TxData);


#endif //__IKY_BLE_APP_H__

