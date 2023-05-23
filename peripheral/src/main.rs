use std::error::Error;
use std::thread::{self};

use rppal::gpio::Gpio;
use rppal::spi::Spi;
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
        36 * 100000,             // 36 Mhz
        rppal::spi::Mode::Mode3, // High on idle, side change on slope down, reading on slope up
    )?;
    println!("{}", adc.ss_polarity()?);
    // Single ended channel 0
    let mut buffer = [0; 3];

    let written = adc.transfer(&mut buffer, &[0b11000 as u8])?;
    assert!(written == 1);
    println!("{buffer:?}");

    Ok(())
}
