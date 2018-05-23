/* 
 * File:   ble_encrypt.h
 * Author: HaiDV
 *
 * Created on July 30, 2014, 2:41 PM
 */

#ifndef BLE_ENCRYPT_H
#define	BLE_ENCRYPT_H
#include <stdint.h>
#include <string.h>

void HashMD5(uint8_t *dst,uint8_t *src,uint16_t srcLen);
void BLE_Decrypt(uint8_t* Dst, uint8_t* MaterKey, uint8_t* planMsg);
uint8_t BLE_CalcCheckSum(uint8_t *buff, uint16_t length);
uint32_t BLE_CalcPINBlock(uint8_t *buff, uint16_t length);


#endif	/* BLE_ENCRYPT_H */

