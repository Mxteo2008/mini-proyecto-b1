package MiniProyecto
import scala.util.{Failure, Success, Try}

object MotorCifrado {


  def procesarMensaje(mensaje: String, clave: Int, cifrado: (Char, Int) => Char): Try[String] =
    Try {
      if (mensaje == null) throw new IllegalArgumentException("El mensaje es nulo.")
      if (mensaje.isEmpty) throw new IllegalArgumentException("El mensaje está vacío.")
      mensaje.map(caracter => cifrado(caracter, clave))
    }


  val cesar: (Char, Int) => Char = (caracter, clave) => {
    if (clave < -25 || clave > 25)
      throw new IllegalArgumentException(s"Clave inválida para César: $clave (debe estar entre -25 y 25).")

    if (caracter >= 'A' && caracter <= 'Z')
      desplazar(caracter, 'A', clave)
    else if (caracter >= 'a' && caracter <= 'z')
      desplazar(caracter, 'a', clave)
    else
      throw new IllegalArgumentException(s"Carácter no soportado en César: '$caracter'")
  }


  private def desplazar(caracter: Char, base: Char, clave: Int): Char =
    (((caracter - base + clave) % 26 + 26) % 26 + base).toChar

  val xor: (Char, Int) => Char = (caracter, clave) => {
    if (clave < 0 || clave > 127)
      throw new IllegalArgumentException(s"Clave inválida para XOR: $clave (debe estar entre 0 y 127).")
    if (caracter.toInt > 127)
      throw new IllegalArgumentException(s"Carácter no soportado en XOR (no es ASCII): '$caracter'")

    (caracter.toInt ^ clave).toChar
  }


  private def mostrar(titulo: String, resultado: Try[String]): Unit =
    resultado match {
      case Success(texto) => println(s"$titulo: $texto")
      case Failure(error) => println(s"$titulo -> ERROR controlado: ${error.getMessage}")
    }

  def main(args: Array[String]): Unit = {

    println("=== Caso César ===")
    val cifradoCesar = procesarMensaje("FUNCIONAL", 3, cesar)
    mostrar("Cifrado   (k = 3)", cifradoCesar)
    mostrar("Descifrado (k = -3)", cifradoCesar.flatMap(texto => procesarMensaje(texto, -3, cesar)))

    println("\n=== Caso XOR ===")
    val cifradoXor = procesarMensaje("scala", 5, xor)
    mostrar("Cifrado   (k = 5)", cifradoXor)
    mostrar("Descifrado (k = 5)", cifradoXor.flatMap(texto => procesarMensaje(texto, 5, xor)))

    println("\n=== Manejo de errores ===")
    mostrar("Mensaje vacío", procesarMensaje("", 3, cesar))
    mostrar("Mensaje nulo", procesarMensaje(null, 3, cesar))
    mostrar("César, clave muy grande (1000)", procesarMensaje("HOLA", 1000, cesar))
    mostrar("César, carácter especial ('1')", procesarMensaje("HOLA1", 3, cesar))
    mostrar("XOR, clave negativa (-1)", procesarMensaje("scala", -1, xor))
    mostrar("XOR, carácter no ASCII ('ñ')", procesarMensaje("año", 5, xor))
  }
}