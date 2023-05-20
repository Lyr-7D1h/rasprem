use std::error::Error;
use std::thread::{self};
use std::time::Duration;

use rppal::gpio::Gpio;
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
    let mut pin = Gpio::new()?.get(23)?.into_output();

    loop {
        println!("Blinking an LED on a {}.", DeviceInfo::new()?.model());
        // Blink the LED by setting the pin's logic level high for 500 ms.
        pin.set_high();
        thread::sleep(Duration::from_millis(500));
        pin.set_low();
        thread::sleep(Duration::from_millis(500));
    }
}
