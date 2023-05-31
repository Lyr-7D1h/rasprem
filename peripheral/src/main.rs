use std::error::Error;
use std::thread::{self};

use rppal::gpio::Gpio;
use rppal::spi::{Polarity, Spi};
use rppal::system::DeviceInfo;

// Gpio uses BCM pin numbering. BCM GPIO 23 is tied to physical pin 16.
const GPIO_SCLK: u8 = 11;
// Master Out Slave In (data output from master)
const GPIO_MOSI: u8 = 10;
// Master In Slave Out (data output from slave)
const GPIO_MISO: u8 = 9;
// Slave Select: output from master to indicate data being sent
const GPIO_SS: u8 = 22;

fn main() -> Result<(), Box<dyn Error>> {
    let adc = Spi::new(
        rppal::spi::Bus::Spi0,
        rppal::spi::SlaveSelect::Ss0,
        1 * 10 * 6,              // 36 Mhz
        rppal::spi::Mode::Mode3, // CPOL 1, CPHA 1:  High on idle, side change on slope down, reading on slope up
    )?;
    assert_eq!(adc.ss_polarity()?, Polarity::ActiveLow);
    // Single ended channel 0
    let mut buffer = [0; 3];
    // start + diff + d2 + d1 + d0
    let command = [0b00000001, 0b11000000, 0];
    println!("{command:?}");

    adc.transfer(&mut buffer, &command)?;
    println!("{buffer:?}");

    Ok(())
}
